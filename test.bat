@echo off
setlocal
pushd "%~dp0"
rem SelfCheck memakai database H2 di memori, jadi data MySQL tidak tersentuh.
set "taskMvn=mvn"
where mvn >nul 2>&1
if errorlevel 1 (
    if exist "C:\Program Files\apache-maven-3.10.0\bin\mvn.cmd" (
        set "taskMvn=C:\Program Files\apache-maven-3.10.0\bin\mvn.cmd"
    ) else if exist "C:\Program Files\NetBeans-17\netbeans\java\maven\bin\mvn.cmd" (
        set "taskMvn=C:\Program Files\NetBeans-17\netbeans\java\maven\bin\mvn.cmd"
    ) else (
        goto gagal
    )
)
call "%taskMvn%" -q -B test-compile exec:java -Dexec.mainClass=laundryku.SelfCheck -Dexec.classpathScope=test
if errorlevel 1 goto gagal
popd
exit /b 0
:gagal
popd
exit /b 1
