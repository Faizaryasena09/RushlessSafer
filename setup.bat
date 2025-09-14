@echo off
:: Get the directory of the script
set "SCRIPT_DIR=%~dp0"

:: Construct the full path to the executable
set "EXE_PATH=%SCRIPT_DIR%bin\Debug\net8.0-windows\RushlessSafer.exe"

:: For the registry file, backslashes need to be escaped (doubled)
set "REG_PATH=%EXE_PATH:\=\%"

:: Create the .reg file content
echo Windows Registry Editor Version 5.00 > register_protocol.reg
echo. >> register_protocol.reg
echo [HKEY_CLASSES_ROOT\rushless-safer] >> register_protocol.reg
echo @="URL:Rushless Safer Protocol" >> register_protocol.reg
echo "URL Protocol"="" >> register_protocol.reg
echo. >> register_protocol.reg
echo [HKEY_CLASSES_ROOT\rushless-safer\shell] >> register_protocol.reg
echo. >> register_protocol.reg
echo [HKEY_CLASSES_ROOT\rushless-safer\shell\open] >> register_protocol.reg
echo. >> register_protocol.reg
echo [HKEY_CLASSES_ROOT\rushless-safer\shell\open\command] >> register_protocol.reg
echo @=""%REG_PATH%" %%1" >> register_protocol.reg

:: Import the .reg file silently
reg import register_protocol.reg

:: Clean up the generated .reg file
del register_protocol.reg

echo.
echo =====================================================
echo  Protokol 'rushless-safer://' berhasil didaftarkan.
echo =====================================================
echo.
pause
