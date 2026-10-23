<#
.SYNOPSIS
  Constroi os artefatos de execucao do E4 a partir de uma worktree num commit especifico.

.DESCRIPTION
  Gera em -Out: jars "boot" de identity, catalog e scheduling-clean; classes e classpath de runtime
  da variante layered (o POM pai desativa o repackage dela); commit.txt com o commit de origem.
  A saida e consumida por run-e4.ps1.

  Mantido em ASCII: o Windows PowerShell 5.1 le scripts sem BOM como ANSI.

.EXAMPLE
  .\experiments\e4-contrato\construir-artefatos.ps1 -Worktree C:\tmp\tcc-e4\wt-base -Out C:\tmp\tcc-e4\jars\base
#>
param([Parameter(Mandatory = $true)][string]$Worktree, [Parameter(Mandatory = $true)][string]$Out)
$ErrorActionPreference = 'Continue'
Set-Location $Worktree
New-Item -ItemType Directory -Force -Path $Out | Out-Null
$mods = 'services/identity-service/infra,services/catalog-service/infra,services/scheduling-service-clean/infra,services/scheduling-service-layered'
& .\mvnw.cmd -B -q package -DskipTests -pl $mods -am 2>&1 | Select-Object -Last 20
& .\mvnw.cmd -B -q dependency:build-classpath -pl services/scheduling-service-layered "-Dmdep.outputFile=$Out\layered\layered.classpath" "-Dmdep.includeScope=runtime" 2>&1 | Select-Object -Last 20
Copy-Item services/identity-service/infra/target/*-boot.jar $Out
Copy-Item services/catalog-service/infra/target/*-boot.jar $Out
Copy-Item services/scheduling-service-clean/infra/target/*-boot.jar $Out
Copy-Item -Recurse services/scheduling-service-layered/target/classes "$Out\layered\classes"
git -C $Worktree log -1 --format='%h %s' | Out-File "$Out\commit.txt" -Encoding ascii
Get-ChildItem -Recurse $Out -Depth 1 -Name
