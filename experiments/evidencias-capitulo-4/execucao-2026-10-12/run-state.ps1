param(
    [Parameter(Mandatory = $true)][string]$Name,
    [Parameter(Mandatory = $true)][string]$Ref,
    [string]$ProdFrom = ''
)
$ErrorActionPreference = 'Continue'
$repo = 'C:\Users\fabio\OneDrive\Documentos\services-tcc'
$root = 'C:\tmp\tcc-wt'
$wt = Join-Path $root $Name
$out = Join-Path $root "out\$Name"
New-Item -ItemType Directory -Force -Path $out | Out-Null

if (Test-Path $wt) { git -C $repo worktree remove --force $wt 2>&1 | Out-Null }
git -C $repo worktree add --detach $wt $Ref 2>&1 | Out-File "$out\worktree.txt"

if ($ProdFrom -ne '') {
    # Aplica somente o diff de producao (src/main, pom.xml, resources, infra/) de $ProdFrom sobre $Ref.
    git -C $repo diff --binary -M "--output=$out\prod.patch" $Ref $ProdFrom -- 'services/*/src/main/*' 'services/*pom.xml' 'infra/*'
    git -C $wt apply --index "$out\prod.patch" 2>&1 | Out-File "$out\apply.txt"
    git -C $wt status --short | Out-File "$out\status.txt"
    git -C $wt diff --cached --numstat -M | Out-File "$out\applied-numstat.txt"
}

Push-Location $wt
$start = Get-Date
$log = & "$wt\mvnw.cmd" -B clean test -fae "-Dmaven.test.failure.ignore=true" 2>&1 | ForEach-Object { "$_" }
$exit = $LASTEXITCODE
$elapsed = (Get-Date) - $start
Pop-Location
$log | Out-File -Encoding utf8 "$out\mvn.log"

$lines = New-Object System.Collections.Generic.List[string]
$lines.Add("# Estado: $Name | Ref: $Ref | ProdFrom: $ProdFrom | Commit: $((git -C $wt rev-parse --short HEAD).Trim())")
$lines.Add("# Data: $((Get-Date).ToString('s')) | Exit Maven: $exit | Duracao: $([int]$elapsed.TotalSeconds)s")
foreach ($r in Get-ChildItem $wt -Recurse -Directory -Filter surefire-reports) {
    $mod = $r.Parent.Parent.FullName.Substring($wt.Length + 1)
    foreach ($x in Get-ChildItem $r.FullName -Filter 'TEST-*.xml') {
        [xml]$xml = Get-Content $x.FullName -Raw -Encoding UTF8
        $s = $xml.testsuite
        $lines.Add("SUITE`t$mod`t$($s.name)`ttests=$($s.tests)`tfail=$($s.failures)`terr=$($s.errors)`tskip=$($s.skipped)")
        foreach ($tc in $s.testcase) {
            foreach ($k in 'failure', 'error') {
                $node = $tc.SelectSingleNode($k)
                if ($node) {
                    $msg = ($node.GetAttribute('message') -replace '\s+', ' ')
                    if ($msg.Length -gt 400) { $msg = $msg.Substring(0, 400) + '...' }
                    $lines.Add("BROKEN`t$k`t$($tc.classname).$($tc.name)`t$($node.GetAttribute('type'))`t$msg")
                }
            }
        }
    }
}
$log | Where-Object { $_ -match 'BUILD (SUCCESS|FAILURE)|Reactor Summary|SUCCESS \[|FAILURE \[|SKIPPED$|COMPILATION ERROR|\.java:\[\d+' } | ForEach-Object { $lines.Add("LOG`t$_") }
$lines | Out-File -Encoding utf8 "$out\summary.txt"
"done $Name exit=$exit"
