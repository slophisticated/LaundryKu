@echo off
setlocal EnableDelayedExpansion
pushd "%~dp0"
set "taskJdk=%JAVA_HOME%"
if not defined taskJdk (
    for /f "tokens=2 delims==" %%j in ('java -XshowSettings:properties -version 2^>^&1 ^| findstr /c:"java.home ="') do set "taskJdk=%%j"
    for /f "tokens=*" %%j in ("!taskJdk!") do set "taskJdk=%%j"
)
if not exist "!taskJdk!\bin\javac.exe" goto gagal
if not exist target\classes mkdir target\classes
if not exist dist mkdir dist
if exist target\sources.txt del target\sources.txt
for /r src\main\java %%f in (*.java) do (
    set "taskSource=%%f"
    echo "!taskSource:\=/!">>target\sources.txt
)
"!taskJdk!\bin\javac.exe" --release 17 -encoding UTF-8 -d target\classes @target\sources.txt
if errorlevel 1 goto gagal
"!taskJdk!\bin\jar.exe" --create --file dist\LaundryKuOOP.jar --main-class laundryku.Main -C target\classes .
if errorlevel 1 goto gagal
echo Build berhasil. Jalankan run.bat atau buka project di NetBeans.
popd
exit /b 0
:gagal
echo Build gagal. Pastikan JDK 17 atau lebih baru tersedia pada PATH.
popd
exit /b 1
