@echo off
setlocal
pushd "%~dp0"
call build.bat
if errorlevel 1 goto gagal
if not exist target\test-classes mkdir target\test-classes
javac --release 17 -encoding UTF-8 -cp target\classes -d target\test-classes src\test\java\laundryku\SelfCheck.java
if errorlevel 1 goto gagal
java -cp "target\classes;target\test-classes" laundryku.SelfCheck
if errorlevel 1 goto gagal
popd
exit /b 0
:gagal
popd
exit /b 1
