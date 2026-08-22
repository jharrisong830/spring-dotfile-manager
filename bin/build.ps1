$RepoRoot = git rev-parse --show-toplevel
$Cwd = Get-Location
Set-Location $RepoRoot

$PomVersion = & .\mvnw.cmd help:evaluate -Dexpression=project.version -q -DforceStdout
Write-Output "BUILD spring-dotfile-manager v$PomVersion"

& .\mvnw.cmd clean package

Set-Location $Cwd
