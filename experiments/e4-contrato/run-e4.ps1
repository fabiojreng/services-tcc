<#
.SYNOPSIS
  Harness do E4: execucao real (sem stubs) apos mudanca de contrato do Catalog.

.DESCRIPTION
  1. Sobe identity (8080), catalog (8082), scheduling-clean (8081) e scheduling-layered (8083)
     com H2, a partir dos artefatos informados (cada um pode vir de um estado diferente).
  2. Seed: usuario TECHNICIAN cadastra um laboratorio (JSON conforme -Contrato do Catalog) e
     usuario PROFESSOR solicita reservas.
  3. Envia -N pedidos de reserva validos a cada variante; registra status, latencia e corpo.
  4. Grava em experiments/e4-contrato/execucao/<Rotulo>/: resultados.csv, excecoes.txt e logs.

  Mantido em ASCII: o Windows PowerShell 5.1 le scripts sem BOM como ANSI.

.EXAMPLE
  .\experiments\e4-contrato\run-e4.ps1 -Rotulo R1 -Contrato e4 -IdentityJar ... -CatalogJar ... -CleanJar ... -LayeredDir ...
#>
param(
    [Parameter(Mandatory = $true)][string]$Rotulo,
    [Parameter(Mandatory = $true)][ValidateSet('original', 'e4', 'controle')][string]$Contrato,
    [Parameter(Mandatory = $true)][string]$IdentityJar,
    [Parameter(Mandatory = $true)][string]$CatalogJar,
    [Parameter(Mandatory = $true)][string]$CleanJar,
    [Parameter(Mandatory = $true)][string]$LayeredDir,
    [int]$N = 10,
    [string]$OutRoot
)

$ErrorActionPreference = 'Continue'
if (-not $OutRoot) { $OutRoot = Join-Path (Split-Path -Parent $MyInvocation.MyCommand.Path) 'execucao' }
$outDir = Join-Path $OutRoot $Rotulo
$logDir = Join-Path $outDir 'logs'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$utf8 = New-Object System.Text.UTF8Encoding($false)

Add-Type -AssemblyName System.Net.Http
$http = New-Object System.Net.Http.HttpClient
$http.Timeout = [TimeSpan]::FromSeconds(30)

function Test-Port([int]$port) {
    try { $c = New-Object Net.Sockets.TcpClient; $c.Connect('localhost', $port); $c.Close(); return $true } catch { return $false }
}

function Wait-Port([int]$port, [int]$timeoutSec = 180) {
    $sw = [Diagnostics.Stopwatch]::StartNew()
    while ($sw.Elapsed.TotalSeconds -lt $timeoutSec) {
        if (Test-Port $port) { return }
        Start-Sleep -Milliseconds 500
    }
    throw "Porta $port nao respondeu em $timeoutSec s"
}

function Send-Json([string]$method, [string]$url, [string]$json) {
    $req = New-Object System.Net.Http.HttpRequestMessage((New-Object System.Net.Http.HttpMethod($method)), $url)
    if ($json) { $req.Content = New-Object System.Net.Http.StringContent($json, [Text.Encoding]::UTF8, 'application/json') }
    $sw = [Diagnostics.Stopwatch]::StartNew()
    $resp = $http.SendAsync($req).GetAwaiter().GetResult()
    $text = $resp.Content.ReadAsStringAsync().GetAwaiter().GetResult()
    $sw.Stop()
    $r = @{ Status = [int]$resp.StatusCode; Body = $text; Ms = [int]$sw.Elapsed.TotalMilliseconds }
    $resp.Dispose()
    return $r
}

$labJson = @{
    original = '{"requesterId":"tec-e4","name":"Lab E4","capacity":30,"opensAt":"08:00","closesAt":"18:00","openDays":["MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY"]}'
    e4       = '{"requesterId":"tec-e4","name":"Lab E4","capacity":30,"openingTime":"08:00","closesAt":"18:00","openDays":["MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY"]}'
    controle = '{"requesterId":"tec-e4","name":"Lab E4","seats":30,"opensAt":"08:00","closesAt":"18:00","openDays":["MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY"]}'
}[$Contrato]

foreach ($p in 8080, 8081, 8082, 8083) {
    if (Test-Port $p) { throw "Porta $p ja esta em uso; encerre o processo antes de executar o harness." }
}
$layeredClasses = Join-Path $LayeredDir 'classes'
$layeredCp = Join-Path $LayeredDir 'layered.classpath'
foreach ($f in $IdentityJar, $CatalogJar, $CleanJar, $layeredClasses, $layeredCp) {
    if (-not (Test-Path $f)) { throw "Artefato ausente: $f" }
}

$procs = @()
function Start-Svc([string]$name, [string[]]$javaArgs) {
    return Start-Process -FilePath 'java' -ArgumentList $javaArgs -PassThru -WindowStyle Hidden `
        -RedirectStandardOutput (Join-Path $logDir "$name.out.log") -RedirectStandardError (Join-Path $logDir "$name.err.log")
}

try {
    $procs += Start-Svc 'identity' @('-jar', "`"$IdentityJar`"")
    $procs += Start-Svc 'catalog' @('-jar', "`"$CatalogJar`"")
    $procs += Start-Svc 'clean' @('-jar', "`"$CleanJar`"")
    $cp = $layeredClasses + ';' + (Get-Content $layeredCp -Raw).Trim()
    $procs += Start-Svc 'layered' @('-cp', "`"$cp`"", 'br.edu.ifma.labmanager.scheduling.layered.SchedulingLayeredApplication')
    foreach ($p in 8080, 8082, 8081, 8083) { Wait-Port $p }
    Write-Host "[$Rotulo] servicos no ar"

    $u1 = Send-Json 'POST' 'http://localhost:8080/api/users' '{"userId":"tec-e4","name":"Tecnico E4","roles":["TECHNICIAN"]}'
    $u2 = Send-Json 'POST' 'http://localhost:8080/api/users' '{"userId":"prof-e4","name":"Professor E4","roles":["PROFESSOR"]}'
    $lab = Send-Json 'POST' 'http://localhost:8082/api/laboratories' $labJson
    if ($lab.Status -ne 201) { throw "Cadastro do laboratorio falhou: $($lab.Status) $($lab.Body)" }
    $labId = ($lab.Body | ConvertFrom-Json).id
    $labGet = Send-Json 'GET' "http://localhost:8082/api/laboratories/$labId" $null
    Write-Host "[$Rotulo] seed: usuarios $($u1.Status)/$($u2.Status), laboratorio $labId"

    $wed = (Get-Date).Date
    do { $wed = $wed.AddDays(1) } while ($wed.DayOfWeek -ne 'Wednesday')
    $wed = $wed.AddDays(7)
    $d = $wed.ToString('yyyy-MM-dd')
    $body = '{"laboratoryId":"' + $labId + '","requesterId":"prof-e4","start":"' + $d + 'T10:00:00","end":"' + $d + 'T12:00:00"}'

    $ports = [ordered]@{ clean = 8081; layered = 8083 }
    $rows = New-Object System.Collections.Generic.List[string]
    $rows.Add('rotulo,variante,i,status,ms,corpo')
    foreach ($v in $ports.Keys) {
        for ($i = 1; $i -le $N; $i++) {
            $r = Send-Json 'POST' "http://localhost:$($ports[$v])/api/reservations" $body
            $corpo = ($r.Body -replace '"', "'" -replace '[\r\n,]', ' ')
            if ($corpo.Length -gt 160) { $corpo = $corpo.Substring(0, 160) }
            $rows.Add("$Rotulo,$v,$i,$($r.Status),$($r.Ms),`"$corpo`"")
        }
    }
    [IO.File]::WriteAllLines((Join-Path $outDir 'resultados.csv'), $rows, $utf8)

    $exc = New-Object System.Collections.Generic.List[string]
    $exc.Add("# E4 | $Rotulo | contrato do Catalog: $Contrato | $((Get-Date).ToString('s'))")
    $exc.Add("# Catalog: $CatalogJar")
    $exc.Add("# Clean: $CleanJar")
    $exc.Add("# Layered: $LayeredDir")
    $exc.Add("# GET /api/laboratories/{id} -> $($labGet.Status) $($labGet.Body)")
    $exc.Add("")
    foreach ($v in $ports.Keys) {
        $statuses = $rows | Select-Object -Skip 1 | Where-Object { $_ -match "^$Rotulo,$v," } | ForEach-Object { ($_ -split ',')[3] }
        $grp = $statuses | Group-Object | ForEach-Object { "$($_.Name) x$($_.Count)" }
        $exc.Add("## $v : $($grp -join ', ')")
        $log = Get-Content (Join-Path $logDir "$v.out.log"), (Join-Path $logDir "$v.err.log") -ErrorAction SilentlyContinue
        $first = $log | Where-Object { $_ -match 'Exception' } | Select-Object -First 3
        $first | ForEach-Object { $exc.Add("    $_") }
        $exc.Add("")
    }
    [IO.File]::WriteAllLines((Join-Path $outDir 'excecoes.txt'), $exc, $utf8)
    $exc | ForEach-Object { Write-Host $_ }
}
finally {
    foreach ($p in $procs) { if ($p) { Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue } }
    $artefatos = @($IdentityJar, $CatalogJar, $CleanJar, $LayeredDir) | ForEach-Object { [regex]::Escape($_) }
    Get-CimInstance Win32_Process -Filter "Name='java.exe'" |
        Where-Object { $cmd = $_.CommandLine; $artefatos | Where-Object { $cmd -match $_ } } |
        ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }
    foreach ($p in 8080, 8081, 8082, 8083) {
        $sw = [Diagnostics.Stopwatch]::StartNew()
        while ((Test-Port $p) -and $sw.Elapsed.TotalSeconds -lt 30) { Start-Sleep -Milliseconds 500 }
    }
    $http.Dispose()
}
