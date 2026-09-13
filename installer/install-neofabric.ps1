param(
    [string]$MinecraftDir = "$env:APPDATA\.minecraft",
    [string]$Artifact = ""
)
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
if ([string]::IsNullOrWhiteSpace($Artifact)) {
    $Artifact = Join-Path $Root "build\libs\neofabric-loader-3.8.0-dev.jar"
}
if (-not (Test-Path $Artifact)) { throw "Artifact not found: $Artifact. Build NeoFabric first." }
$InstallDir = Join-Path $MinecraftDir "neofabric"
New-Item -ItemType Directory -Force $InstallDir | Out-Null
Copy-Item $Artifact (Join-Path $InstallDir "neofabric-loader-dev.jar") -Force
@"
NeoFabric development installation
Loader artifact: neofabric-loader-dev.jar
Source artifact: $Artifact

This is a loader staging install. It does not yet create an official Minecraft
Launcher version profile because NeoFabric still needs its version JSON,
Minecraft libraries, mappings, and launcher bootstrap integration.
"@ | Set-Content (Join-Path $InstallDir "INSTALL-MANIFEST.txt")
Write-Host "Installed NeoFabric staging artifact to $InstallDir"
