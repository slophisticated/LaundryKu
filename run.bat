@echo off
setlocal
pushd "%~dp0"
if not exist dist\LaundryKuOOP.jar call build.bat
if errorlevel 1 goto akhir
java -jar dist\LaundryKuOOP.jar
if errorlevel 1 pause
:akhir
popd
