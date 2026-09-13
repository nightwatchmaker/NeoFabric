param(
    [string]$MinecraftDir = "$env:APPDATA\.minecraft",
    [string]$Artifact = "",
    [string]$MinecraftVersion = "26.2",
    [string]$ProfileId = "NeoFabric-26.2-dev"
)
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
if ([string]::IsNullOrWhiteSpace($Artifact)) {
    $Artifact = Join-Path $Root "build\libs\neofabric-loader-3.9.0-dev.jar"
}
if (-not (Test-Path -LiteralPath $Artifact -PathType Leaf)) {
    throw "NeoFabric artifact not found: $Artifact. Build NeoFabric first."
}
$Artifact = (Resolve-Path -LiteralPath $Artifact).Path
$ManifestUrl = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
$Work = Join-Path ([System.IO.Path]::GetTempPath()) ("neofabric-official-" + [guid]::NewGuid())
$VersionDir = Join-Path $MinecraftDir ("versions\" + $ProfileId)
$LibraryDir = Join-Path $MinecraftDir "libraries\org\neofabric\loader\3.9.0-dev"
try {
    New-Item -ItemType Directory -Force $Work, $VersionDir, $LibraryDir | Out-Null
    $Manifest = Join-Path $Work "version_manifest.json"
    Invoke-WebRequest -Uri $ManifestUrl -OutFile $Manifest -UseBasicParsing
    $ManifestJson = Get-Content -LiteralPath $Manifest -Raw | ConvertFrom-Json
    $Version = $ManifestJson.versions | Where-Object { $_.id -eq $MinecraftVersion } | Select-Object -First 1
    if ($null -eq $Version) { throw "Minecraft version $MinecraftVersion was not found in Mojang's manifest." }
    $BaseJson = Join-Path $Work "$MinecraftVersion.json"
    Invoke-WebRequest -Uri $Version.url -OutFile $BaseJson -UseBasicParsing
    $Generator = Join-Path $Root "installer\generate-official-profile.py"
    & python $Generator --base-json $BaseJson --loader-jar $Artifact --output-dir $VersionDir --id $ProfileId
    if ($LASTEXITCODE -ne 0) { throw "Official profile generator failed with exit code $LASTEXITCODE." }
    Copy-Item -LiteralPath $Artifact -Destination (Join-Path $LibraryDir "loader-3.9.0-dev.jar") -Force
    $ProfileJson = Join-Path $VersionDir "$ProfileId.json"
    $Profile = Get-Content -LiteralPath $ProfileJson -Raw | ConvertFrom-Json
    $Profile.neoFabric.status = "development-profile-generated"
    $Profile.neoFabric.targetMinecraftVersion = $MinecraftVersion
    $Profile | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath $ProfileJson -Encoding UTF8
    $Jar = Join-Path $LibraryDir "loader-3.9.0-dev.jar"
    $Hash = (Get-FileHash -Algorithm SHA256 -LiteralPath $Jar).Hash.ToLowerInvariant()
    @(
        "NeoFabric official-launcher development profile"
        "Profile: $ProfileId"
        "Minecraft: $MinecraftVersion"
        "Profile JSON: $ProfileJson"
        "Loader library: $Jar"
        "Loader SHA-256: $Hash"
        "Status: development-only; use the official launcher profile only for bootstrap testing"
    ) | Set-Content -LiteralPath (Join-Path $VersionDir "INSTALL-MANIFEST.txt") -Encoding UTF8
    Write-Host "Generated official Minecraft Launcher profile: $ProfileJson"
    Write-Host "Installed NeoFabric library: $Jar"
    Write-Host "SHA-256: $Hash"
} finally {
    if (Test-Path -LiteralPath $Work) { Remove-Item -LiteralPath $Work -Recurse -Force }
}
