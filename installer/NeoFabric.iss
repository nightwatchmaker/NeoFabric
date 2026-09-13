; NeoFabric Inno Setup source.
; Build this on Windows with Inno Setup after the official-launcher profile is ready.
#define AppName "NeoFabric Development Loader"
#define AppVersion "1.5.0-dev"
#define AppPublisher "NeoFabric Project"

[Setup]
AppId={{A7E5D7B1-8E7F-4E0B-9EA6-260226020001}
AppName={#AppName}
AppVersion={#AppVersion}
AppPublisher={#AppPublisher}
DefaultDirName={userappdata}\.minecraft\neofabric
DisableProgramGroupPage=yes
OutputBaseFilename=NeoFabric-Installer-{#AppVersion}
Compression=lzma
SolidCompression=yes
PrivilegesRequired=lowest

[Files]
Source: "..\build\libs\neofabric-loader-3.8.0-dev.jar"; DestDir: "{app}"; DestName: "neofabric-loader-dev.jar"; Flags: ignoreversion
Source: "install-neofabric.ps1"; DestDir: "{app}"; Flags: ignoreversion

[Code]
function InitializeSetup(): Boolean;
begin
  MsgBox('This installer stages the NeoFabric development loader. It does not yet create an official Minecraft Launcher version profile.', mbInformation, MB_OK);
  Result := True;
end;
