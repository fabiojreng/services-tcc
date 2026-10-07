<#
.SYNOPSIS
  Harness do E2-v2: falhas de rede reais (Toxiproxy) entre Agendamento e Identity/Catalog.

.DESCRIPTION
  1. Garante o container toxiproxy (infra/docker-compose.yml).
  2. Sobe identity (8080), catalog (8082), scheduling-clean (8081) e scheduling-layered (8083)
     a partir dos jars "boot" em -JarDir. As variantes de Agendamento usam o perfil e2
     (Identity via localhost:18080, Catalog via localhost:18082).
  3. Seed: usuario TECHNICIAN (cadastra laboratorio) e PROFESSOR (solicita reservas).
  4. Para cada cenario S0..S4 e variante: aplica o toxic, dispara requisicoes sequenciais
     e registra status e latencia (cliente com timeout de -ClientTimeoutSec).
  5. Grava resultados-fase-<Fase>.csv e resumo-fase-<Fase>.csv em experiments/e2-falhas-v2/.

  Mantido em ASCII: o Windows PowerShell 5.1 le scripts sem BOM como ANSI.

.EXAMPLE
  .\experiments\e2-falhas-v2\run-e2.ps1 -Fase A -JarDir $env:TEMP\e2-jars-A
#>
param(
    [Parameter(Mandatory = $true)][ValidateSet('A', 'B')][string]$Fase,
    [Parameter(Mandatory = $true)][string]$JarDir,
    [int]$N = 30,
    [int]$NTimeout = 10,
    [int]$ClientTimeoutSec = 60,
    [string[]]$Cenarios = @('S0', 'S1', 'S2', 'S3', 'S4')
)

$ErrorActionPreference = 'Continue'
$repo = (git rev-parse --show-toplevel).Trim()
$outDir = Join-Path $repo 'experiments/e2-falhas-v2'
$logDir = Join-Path $outDir "logs-fase-$Fase"
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$toxi = 'http://localhost:8474'
$utf8 = New-Object System.Text.UTF8Encoding($false)

Add-Type -AssemblyName System.Net.Http
$http = New-Object System.Net.Http.HttpClient
$http.Timeout = [TimeSpan]::FromSeconds($ClientTimeoutSec)

function Test-Port([int]$port) {
    try { $c = New-Object Net.Sockets.TcpClient; $c.Connect('localhost', $port); $c.Close(); return $true } catch { return $false }
}

function Wait-Port([int]$port, [int]$timeoutSec = 120) {
    $sw = [Diagnostics.Stopwatch]::StartNew()
    while ($sw.Elapsed.TotalSeconds -lt $timeoutSec) {
        if (Test-Port $port) { return }
        Start-Sleep -Milliseconds 500
    }
    throw "Porta $port nao respondeu em $timeoutSec s"
}

# HttpClient em vez de Invoke-RestMethod: o Toxiproxy recusa o user agent "Mozilla" do PowerShell 5.1.
$admin = New-Object System.Net.Http.HttpClient
$admin.Timeout = [TimeSpan]::FromSeconds(30)

function Invoke-Json([string]$method, [string]$url, [string]$json) {
    $req = New-Object System.Net.Http.HttpRequestMessage((New-Object System.Net.Http.HttpMethod($method)), $url)
    if ($json) { $req.Content = New-Object System.Net.Http.StringContent($json, [Text.Encoding]::UTF8, 'application/json') }
    $resp = $admin.SendAsync($req).GetAwaiter().GetResult()
    $text = $resp.Content.ReadAsStringAsync().GetAwaiter().GetResult()
    if (-not $resp.IsSuccessStatusCode) { throw "$method $url -> $([int]$resp.StatusCode): $text" }
    $resp.Dispose()
    if ($text) { return $text | ConvertFrom-Json }
}

function Invoke-Toxi([string]$method, [string]$path, $body) {
    $json = if ($null -ne $body) { $body | ConvertTo-Json -Depth 5 -Compress } else { $null }
    Invoke-Json $method "$toxi$path" $json | Out-Null
}

function Reset-Toxi { Invoke-Toxi 'POST' '/reset' $null }

$scenarioSetup = @{
    'S0' = { }
    'S1' = { Invoke-Toxi 'POST' '/proxies/catalog/toxics' @{ name = 'lat'; type = 'latency'; stream = 'downstream'; attributes = @{ latency = 500; jitter = 0 } } }
    'S2' = { Invoke-Toxi 'POST' '/proxies/catalog/toxics' @{ name = 'lat'; type = 'latency'; stream = 'downstream'; attributes = @{ latency = 5000; jitter = 0 } } }
    'S3' = { Invoke-Toxi 'POST' '/proxies/identity' @{ enabled = $false } }
    'S4' = { Invoke-Toxi 'POST' '/proxies/catalog/toxics' @{ name = 'hang'; type = 'timeout'; stream = 'downstream'; attributes = @{ timeout = 0 } } }
}

# --- Infraestrutura ---
docker compose -f (Join-Path $repo 'infra/docker-compose.yml') up -d toxiproxy 2>&1 | Out-Null
Wait-Port 8474 60

foreach ($p in 8080, 8081, 8082, 8083) {
    if (Test-Port $p) { throw "Porta $p ja esta em uso; encerre o processo antes de executar o harness." }
}

$jars = @{
    identity = Get-ChildItem $JarDir -Filter 'identity-infra-*-boot.jar' | Select-Object -First 1
    catalog  = Get-ChildItem $JarDir -Filter 'catalog-infra-*-boot.jar' | Select-Object -First 1
    clean    = Get-ChildItem $JarDir -Filter 'scheduling-clean-infra-*-boot.jar' | Select-Object -First 1
}
foreach ($k in $jars.Keys) { if (-not $jars[$k]) { throw "Jar de $k nao encontrado em $JarDir" } }
# A variante layered nao gera jar "boot" (o POM pai desativa o repackage); executa por classpath.
$layeredClasses = Join-Path $JarDir 'layered-classes'
$layeredCp = Join-Path $JarDir 'layered.classpath'
if (-not (Test-Path $layeredClasses) -or -not (Test-Path $layeredCp)) { throw "layered-classes/layered.classpath ausentes em $JarDir" }

$procs = @()
function Start-Svc([string]$name, [string[]]$javaArgs) {
    $p = Start-Process -FilePath 'java' -ArgumentList $javaArgs -PassThru -WindowStyle Hidden `
        -RedirectStandardOutput (Join-Path $logDir "$name.out.log") -RedirectStandardError (Join-Path $logDir "$name.err.log")
    return $p
}

try {
    $procs += Start-Svc 'identity' @('-jar', $jars.identity.FullName)
    $procs += Start-Svc 'catalog' @('-jar', $jars.catalog.FullName)
    $procs += Start-Svc 'clean' @('-jar', $jars.clean.FullName, '--spring.profiles.active=e2')
    $cp = $layeredClasses + ';' + (Get-Content $layeredCp -Raw).Trim()
    $procs += Start-Svc 'layered' @('-cp', "`"$cp`"", 'br.edu.ifma.labmanager.scheduling.layered.SchedulingLayeredApplication', '--spring.profiles.active=e2')
    foreach ($p in 8080, 8082, 8081, 8083) { Wait-Port $p 180 }
    Write-Host "Servicos no ar"
    Reset-Toxi
    Write-Host "Toxiproxy resetado"

    # --- Seed ---
    Invoke-Json 'POST' 'http://localhost:8080/api/users' '{"userId":"tec-e2","name":"Tecnico E2","roles":["TECHNICIAN"]}' | Out-Null
    Invoke-Json 'POST' 'http://localhost:8080/api/users' '{"userId":"prof-e2","name":"Professor E2","roles":["PROFESSOR"]}' | Out-Null
    $lab = Invoke-Json 'POST' 'http://localhost:8082/api/laboratories' '{"requesterId":"tec-e2","name":"Lab E2","capacity":30,"opensAt":"08:00","closesAt":"18:00","openDays":["MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY"]}'
    $labId = $lab.id
    Write-Host "Seed concluido (laboratorio $labId)"

    $wed = (Get-Date).Date
    do { $wed = $wed.AddDays(1) } while ($wed.DayOfWeek -ne 'Wednesday')
    $wed = $wed.AddDays(7)
    $body = '{"laboratoryId":"' + $labId + '","requesterId":"prof-e2","start":"' + $wed.ToString('yyyy-MM-dd') + 'T10:00:00","end":"' + $wed.ToString('yyyy-MM-dd') + 'T12:00:00"}'

    $ports = [ordered]@{ clean = 8081; layered = 8083 }

    function Send-Reservation([int]$port) {
        $sw = [Diagnostics.Stopwatch]::StartNew()
        $status = ''
        try {
            $content = New-Object System.Net.Http.StringContent($body, [Text.Encoding]::UTF8, 'application/json')
            $resp = $http.PostAsync("http://localhost:$port/api/reservations", $content).GetAwaiter().GetResult()
            $status = [string][int]$resp.StatusCode
            $resp.Dispose()
        } catch [System.Threading.Tasks.TaskCanceledException] {
            $status = 'CLIENT_TIMEOUT'
        } catch {
            $status = 'CLIENT_ERROR'
        }
        $sw.Stop()
        return @{ Status = $status; Ms = [int]$sw.Elapsed.TotalMilliseconds }
    }

    # Aquecimento (nao registrado): JIT e pools de conexao
    foreach ($v in $ports.Keys) { 1..3 | ForEach-Object { Send-Reservation $ports[$v] | Out-Null } }

    $rows = New-Object System.Collections.Generic.List[string]
    $rows.Add('fase,variante,cenario,i,status,ms')
    foreach ($s in $Cenarios) {
        $count = if ($s -eq 'S4') { $NTimeout } else { $N }
        foreach ($v in $ports.Keys) {
            Reset-Toxi
            & $scenarioSetup[$s]
            Write-Host "Fase $Fase | $s | $v | n=$count"
            for ($i = 1; $i -le $count; $i++) {
                $r = Send-Reservation $ports[$v]
                $rows.Add("$Fase,$v,$s,$i,$($r.Status),$($r.Ms)")
            }
            Reset-Toxi
            Start-Sleep -Seconds 2
        }
    }
    [IO.File]::WriteAllLines((Join-Path $outDir "resultados-fase-$Fase.csv"), $rows, $utf8)

    # --- Resumo ---
    $data = Import-Csv (Join-Path $outDir "resultados-fase-$Fase.csv")
    $summary = New-Object System.Collections.Generic.List[string]
    $summary.Add('fase,variante,cenario,n,pct_201,pct_503,pct_outro_http,pct_client_timeout,p50_ms,p95_ms,max_ms')
    foreach ($g in ($data | Group-Object variante, cenario)) {
        $items = $g.Group
        $n = $items.Count
        $ms = @($items | ForEach-Object { [int]$_.ms } | Sort-Object)
        $p50 = $ms[[Math]::Floor(0.50 * ($n - 1))]
        $p95 = $ms[[Math]::Floor(0.95 * ($n - 1))]
        $c201 = @($items | Where-Object status -eq '201').Count
        $c503 = @($items | Where-Object status -eq '503').Count
        $cto = @($items | Where-Object status -eq 'CLIENT_TIMEOUT').Count
        $cother = $n - $c201 - $c503 - $cto
        $pct = { param($x) [Math]::Round(100.0 * $x / $n, 1).ToString([Globalization.CultureInfo]::InvariantCulture) }
        $summary.Add("$Fase,$($items[0].variante),$($items[0].cenario),$n,$(& $pct $c201),$(& $pct $c503),$(& $pct $cother),$(& $pct $cto),$p50,$p95,$($ms[-1])")
    }
    [IO.File]::WriteAllLines((Join-Path $outDir "resumo-fase-$Fase.csv"), $summary, $utf8)
    $summary | ForEach-Object { Write-Host $_ }
}
finally {
    try { Reset-Toxi } catch { }
    foreach ($p in $procs) { if ($p -and -not $p.HasExited) { Stop-Process -Id $p.Id -Force } }
    $http.Dispose()
    $admin.Dispose()
}
