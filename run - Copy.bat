@echo off
setlocal
echo ======================================================
echo   Starting Student Result Management System...
echo ======================================================

set "JAVA_CMD=java"
if exist "C:\Program Files\Java\jdk-26.0.1\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Java\jdk-26.0.1\bin\java.exe"
)

"%JAVA_CMD%" -cp "target/classes;lib/mysql-connector-j-8.3.0.jar;lib/*;target/dependency/*" result.ui.MainApplication
pause
