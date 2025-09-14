
; Script for RushlessSafer Installer

!define APP_NAME "Rushless Safer"
!define APP_VERSION "1.0"
!define EXE_NAME "Rushless Safer.exe"
!define REG_FILE "register_protocol.reg"

OutFile "${APP_NAME}-Installer.exe"
InstallDir "$PROGRAMFILES\${APP_NAME}"
RequestExecutionLevel admin

Page directory
Page instfiles

Section "Install"
  SetOutPath $INSTDIR
  
  ; Add application files
  File /r "bin\Debug\net8.0-windows\*.*"
  
  ; Add registry keys
  WriteRegStr HKCR "rushless-safer" "" "URL:Rushless Safer Protocol"
  WriteRegStr HKCR "rushless-safer" "URL Protocol" ""
  WriteRegStr HKCR "rushless-safer\shell\open\command" "" '"$INSTDIR\${EXE_NAME}" "%1"'
  
  ; Create Desktop Shortcut
  CreateShortCut "$DESKTOP\${APP_NAME}.lnk" "$INSTDIR\${EXE_NAME}"
  
  ; Write the uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"
SectionEnd

Section "Uninstall"
  ; Remove registry keys
  DeleteRegKey HKCR "rushless-safer"
  
  Delete "$INSTDIR\*.*"
  Delete "$DESKTOP\${APP_NAME}.lnk"
  
  RMDir /r "$INSTDIR"
  
SectionEnd
