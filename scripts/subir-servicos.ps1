<#
.SYNOPSIS
  Sobe (ou para) os cinco microsservicos e o console web em segundo plano.

.DESCRIPTION
  Empacota o projeto, inicia cada servico como processo Java oculto e aguarda as portas responderem.
  Logs em logs/<servico>.log. A variante layered nao gera jar executavel (repackage desligado no POM raiz),
  entao roda pelo classpath montado pelo Maven.

  O script e mantido em ASCII: o Windows PowerShell 5.1 le scripts sem BOM como ANSI.

.EXAMPLE
  .\scripts\subir-servicos.ps1                     # H2 em memoria
  .\scripts\subir-servicos.ps1 -Perfil postgres    # exige: docker compose -f infra/docker-compose.yml up -d
  .\scripts\subir-servicos.ps1 -Parar
#>
param(
    [ValidateSet('h2', 'postgres')][string]$Perfil = 'h2',
    [switch]$SemBuild,
    [switch]$SemConsole,
    [switch]$Parar
)

$ErrorActionPreference = 'Continue'
$repo = Split-Path -Parent $PSScriptRoot
Set-Location $repo
$logDir = Join-Path $repo 'logs'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$marker = '-Dlabmanager.launcher=subir-servicos'

function Get-Started {
    Get-CimInstance Win32_Process -Filter "Name='java.exe'" |
        Where-Object { $_.CommandLine -and $_.CommandLine.Contains($marker) }
}

function Test-Port([int]$port) {
    $client = New-Object Net.Sockets.TcpClient
    try {
        $client.ConnectAsync('127.0.0.1', $port).Wait(300) -and $client.Connected
    } catch {
        $false
    } finally {
        $client.Dispose()
    }
}

if ($Parar) {
    $running = @(Get-Started)
    $running | ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }
    Write-Host "Processos encerrados: $($running.Count)"
    return
}

$layeredCp = Join-Path $logDir 'layered-classpath.txt'
if (-not $SemBuild) {
    Write-Host 'Empacotando (mvnw package -DskipTests)...'
    & "$repo\mvnw.cmd" -q -B package -DskipTests
    if ($LASTEXITCODE -ne 0) { Write-Host 'Falha no build.' -ForegroundColor Red; return }
    & "$repo\mvnw.cmd" -q -B -pl services/scheduling-service-layered dependency:build-classpath `
        "-Dmdep.outputFile=$layeredCp" '-Dmdep.includeScope=runtime'
}

$profileArgs = if ($Perfil -eq 'postgres') { @('--spring.profiles.active=postgres') } else { @() }
$layeredClasspath = 'services/scheduling-service-layered/target/classes;' + (Get-Content $layeredCp -Raw).Trim()

$services = @(
    @{ Name = 'identity'; Port = 8080; Args = @('-jar', 'services/identity-service/infra/target/identity-infra-0.1.0-SNAPSHOT-boot.jar') + $profileArgs },
    @{ Name = 'catalog'; Port = 8082; Args = @('-jar', 'services/catalog-service/infra/target/catalog-infra-0.1.0-SNAPSHOT-boot.jar') + $profileArgs },
    @{ Name = 'scheduling-clean'; Port = 8081; Args = @('-jar', 'services/scheduling-service-clean/infra/target/scheduling-clean-infra-0.1.0-SNAPSHOT-boot.jar') + $profileArgs },
    @{ Name = 'scheduling-layered'; Port = 8083; Args = @('-cp', "`"$layeredClasspath`"", 'br.edu.ifma.labmanager.scheduling.layered.SchedulingLayeredApplication') + $profileArgs },
    @{ Name = 'inventory'; Port = 8084; Args = @('-jar', 'services/inventory-service/infra/target/inventory-infra-0.1.0-SNAPSHOT-boot.jar') + $profileArgs }
)
if (-not $SemConsole) {
    $services += @{ Name = 'web-console'; Port = 8090; Args = @('-jar', 'web-console/target/web-console-0.1.0-SNAPSHOT-boot.jar') }
}

foreach ($service in $services) {
    if (Test-Port $service.Port) {
        Write-Host "$($service.Name): porta $($service.Port) ja em uso, nao iniciado." -ForegroundColor Yellow
        $service.Skipped = $true
        continue
    }
    Start-Process java -ArgumentList (@($marker) + $service.Args) -WindowStyle Hidden `
        -RedirectStandardOutput (Join-Path $logDir "$($service.Name).log") `
        -RedirectStandardError (Join-Path $logDir "$($service.Name).err.log") | Out-Null
}

Write-Host "Aguardando os servicos (perfil $Perfil)..."
$deadline = (Get-Date).AddSeconds(90)
do {
    Start-Sleep -Seconds 2
    $pending = @($services | Where-Object { -not $_.Skipped -and -not (Test-Port $_.Port) })
} while ($pending.Count -gt 0 -and (Get-Date) -lt $deadline)

foreach ($service in $services) {
    if ($service.Skipped) { continue }
    if (Test-Port $service.Port) {
        Write-Host ("  OK     {0,-20} http://localhost:{1}" -f $service.Name, $service.Port) -ForegroundColor Green
    } else {
        Write-Host ("  FALHOU {0,-20} veja logs/{0}.log" -f $service.Name) -ForegroundColor Red
        Get-Content (Join-Path $logDir "$($service.Name).log") -Tail 15 -ErrorAction SilentlyContinue |
            ForEach-Object { "         $_" }
    }
}
Write-Host 'Console: http://localhost:8090  |  Parar: .\scripts\subir-servicos.ps1 -Parar'
