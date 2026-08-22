$RepoRoot = git rev-parse --show-toplevel
$Cwd = Get-Location
Set-Location $RepoRoot

& "$RepoRoot\bin\build.ps1"
if ($LASTEXITCODE -ne 0) {
    Set-Location $Cwd
    exit 1
}

$PomVersion = & .\mvnw.cmd help:evaluate "-Dexpression=project.version" -q -DforceStdout
Write-Output "INSTALL spring-dotfile-manager v$PomVersion"

$InstallDir = "$env:LOCALAPPDATA\spring-dotfile-manager\bin"
$JarDir = "$InstallDir\jar"

Write-Output "Installing spring-dotfile-manager to $InstallDir\sdfm.cmd"
Write-Output "Add $InstallDir to your PATH if it's not already there!"

$SourceJar = "$RepoRoot\target\spring-dotfile-manager-$PomVersion.jar"
if (-not (Test-Path $SourceJar)) {
    Write-Error "Built jar not found at $SourceJar (resolved version: '$PomVersion') - aborting install."
    Set-Location $Cwd
    exit 1
}

New-Item -ItemType Directory -Force -Path $InstallDir | Out-Null
New-Item -ItemType Directory -Force -Path $JarDir | Out-Null
Copy-Item $SourceJar "$JarDir\sdfm.jar" -Force
Copy-Item "$RepoRoot\bin\sdfm.cmd" "$InstallDir\sdfm.cmd" -Force

Write-Output "Installed!"

Set-Location $Cwd
