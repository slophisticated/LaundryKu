@echo off
setlocal
pushd "%~dp0"
rem Build memakai Maven karena project membutuhkan library Hibernate, MySQL, dan FlatLaf.
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
call "%taskMvn%" -q -B package
if errorlevel 1 goto gagal
echo Build berhasil: dist\LaundryKuOOP.jar
popd
exit /b 0
:gagal
echo Build gagal. Pastikan JDK 17+ dan Maven tersedia (atau buka project di NetBeans).
popd
exit /b 1
