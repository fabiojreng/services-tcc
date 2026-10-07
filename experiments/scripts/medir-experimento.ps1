<#
.SYNOPSIS
  Instrumento de medicao do protocolo de dois commits (Fase 6).

.DESCRIPTION
  Etapa "producao": HEAD deve ser o commit P (somente codigo de producao).
    - diff-producao.txt      git diff --numstat <Base> HEAD (services/, acceptance/, infra/)
    - mvn-producao.log       saida de "mvnw clean test" nos modulos da variante
    - testes-quebrados.txt   testes que falharam/erraram ou modulos que nao compilaram

  Etapa "testes": HEAD deve ser o commit T (somente ajuste de testes), filho de P.
    - diff-testes.txt        git diff --numstat HEAD~1 HEAD
    - mvn-testes.log / testes-final.txt
    - archunit.txt           regras ArchUnit (todas) + exportacao de metricas
    - metricas.csv           Martin/Lakos com rotulo <Experimento>/<Variante>

  Arquivos gravados em UTF-8 sem BOM em experiments/<Experimento>/<Variante>/.
  O script e mantido em ASCII: o Windows PowerShell 5.1 le scripts sem BOM como ANSI.

.EXAMPLE
  .\experiments\scripts\medir-experimento.ps1 -Experimento e3-regra-v2 -Variante clean -Etapa producao
#>
param(
    [Parameter(Mandatory = $true)][string]$Experimento,
    [Parameter(Mandatory = $true)][ValidateSet('clean', 'layered')][string]$Variante,
    [Parameter(Mandatory = $true)][ValidateSet('producao', 'testes')][string]$Etapa,
    [string]$Base = 'baseline-v2'
)

# 'Continue': no Windows PowerShell 5.1, stderr de comandos nativos com 2>&1 vira erro terminante sob 'Stop'.
$ErrorActionPreference = 'Continue'
$repo = (git rev-parse --show-toplevel).Trim()
Set-Location $repo

$outDir = Join-Path $repo "experiments/$Experimento/$Variante"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
$utf8 = New-Object System.Text.UTF8Encoding($false)

function Write-Utf8([string]$name, [string[]]$lines) {
    if ($null -eq $lines) { $lines = @() }
    [System.IO.File]::WriteAllLines((Join-Path $outDir $name), $lines, $utf8)
}

$modules = if ($Variante -eq 'clean') {
    @('services/scheduling-service-clean/domain',
      'services/scheduling-service-clean/application',
      'services/scheduling-service-clean/presentation',
      'services/scheduling-service-clean/infra')
} else {
    @('services/scheduling-service-layered')
}

function Invoke-VariantTests([string]$logName) {
    $pl = $modules -join ','
    # failure.ignore: falha de teste em um modulo nao impede a execucao dos testes dos modulos dependentes.
    $log = & "$repo\mvnw.cmd" -B clean test -fae "-Dmaven.test.failure.ignore=true" -pl $pl -am 2>&1 |
        ForEach-Object { "$_" }
    $exit = $LASTEXITCODE
    Write-Utf8 $logName $log
    return @{ Log = $log; Exit = $exit }
}

function Get-TestSummary($result) {
    $lines = New-Object System.Collections.Generic.List[string]
    $lines.Add("# Experimento: $Experimento | Variante: $Variante | Etapa: $Etapa")
    $lines.Add("# Commit: $((git rev-parse --short HEAD).Trim()) | Base: $Base | Data: $((Get-Date).ToString('s'))")
    $lines.Add("# Exit code Maven: $($result.Exit)")
    $lines.Add("")

    $total = 0; $fail = 0; $err = 0; $skip = 0
    $broken = New-Object System.Collections.Generic.List[string]
    $perModule = New-Object System.Collections.Generic.List[string]
    foreach ($m in $modules) {
        $reports = Join-Path $repo "$m/target/surefire-reports"
        if (-not (Test-Path $reports)) { $perModule.Add("$m : sem relatorio surefire"); continue }
        $mt = 0; $mb = 0
        foreach ($xmlFile in Get-ChildItem $reports -Filter 'TEST-*.xml') {
            [xml]$xml = Get-Content $xmlFile.FullName -Raw -Encoding UTF8
            $suite = $xml.testsuite
            $total += [int]$suite.tests; $fail += [int]$suite.failures
            $err += [int]$suite.errors; $skip += [int]$suite.skipped
            $mt += [int]$suite.tests; $mb += [int]$suite.failures + [int]$suite.errors
            foreach ($tc in $suite.testcase) {
                if ($tc.failure) { $broken.Add("FAILURE  $($tc.classname).$($tc.name)") }
                if ($tc.error) { $broken.Add("ERROR    $($tc.classname).$($tc.name)") }
            }
        }
        $perModule.Add("$m : executados=$mt quebrados=$mb")
    }

    $compile = $result.Log | Where-Object { $_ -match 'COMPILATION ERROR|Failed to execute goal .*compiler' }
    $compileErrors = $result.Log | Where-Object { $_ -match '^\[ERROR\] .*\.java:\[\d+' }

    $lines.Add("testes_executados=$total")
    $lines.Add("falhas=$fail")
    $lines.Add("erros=$err")
    $lines.Add("ignorados=$skip")
    $lines.Add("quebrados=$($fail + $err)")
    $lines.Add("falha_de_compilacao=$(if ($compile) { 'sim' } else { 'nao' })")
    $lines.Add("")
    $lines.Add("## Por modulo")
    $perModule | ForEach-Object { $lines.Add($_) }
    $lines.Add("")
    if ($broken.Count -gt 0) {
        $lines.Add("## Testes quebrados")
        $broken | ForEach-Object { $lines.Add($_) }
        $lines.Add("")
    }
    if ($compileErrors) {
        $lines.Add("## Erros de compilacao (testes nao chegaram a executar)")
        $compileErrors | ForEach-Object { $lines.Add($_) }
        $lines.Add("")
    }
    $modsSkipped = $result.Log | Where-Object { $_ -match 'SKIPPED$' }
    if ($modsSkipped) {
        $lines.Add("## Modulos nao executados")
        $modsSkipped | ForEach-Object { $lines.Add($_) }
    }
    return $lines
}

$paths = @('services', 'acceptance', 'infra')

if ($Etapa -eq 'producao') {
    $numstat = git diff --numstat $Base HEAD -- $paths
    Write-Utf8 'diff-producao.txt' $numstat
    $result = Invoke-VariantTests 'mvn-producao.log'
    Write-Utf8 'testes-quebrados.txt' (Get-TestSummary $result)
    Write-Host "Etapa producao registrada em $outDir (exit Maven: $($result.Exit))"
}
else {
    $numstat = git diff --numstat HEAD~1 HEAD -- $paths
    Write-Utf8 'diff-testes.txt' $numstat
    $result = Invoke-VariantTests 'mvn-testes.log'
    Write-Utf8 'testes-final.txt' (Get-TestSummary $result)

    $label = "$Experimento/$Variante"
    $arch = & "$repo\mvnw.cmd" -B test -pl architecture-metrics -am "-Dmetrics.label=$label" `
        "-Dtest=*ArchitectureRulesTest,MetricsExportTest" "-Dsurefire.failIfNoSpecifiedTests=false" 2>&1 |
        ForEach-Object { "$_" }
    $archExit = $LASTEXITCODE
    $archSummary = @("# ArchUnit apos $label | exit=$archExit") +
        ($arch | Where-Object { $_ -cmatch 'Tests run:|Violation|was violated|BUILD (SUCCESS|FAILURE)|publicadas' })
    Write-Utf8 'archunit.txt' $archSummary
    Write-Host "Etapa testes registrada em $outDir (exit testes: $($result.Exit), exit ArchUnit: $archExit)"
}
