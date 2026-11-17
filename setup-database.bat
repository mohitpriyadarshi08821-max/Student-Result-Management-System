@echo off
setlocal
echo ========================================================
echo   Student Result Management System - Database Setup
echo ========================================================
echo.
echo MySQL Server (MySQL80) is currently RUNNING on your PC.
echo.
echo Please enter your MySQL 'root' password below.
echo (If you do not have a password, simply press Enter)
echo.
set /p DBPASS="MySQL root password: "

powershell -Command "$p = '%DBPASS%'; (Get-Content 'src\main\resources\database.properties') -replace '^db\.password=.*', ('db.password=' + $p) | Set-Content 'src\main\resources\database.properties'; Copy-Item 'src\main\resources\database.properties' 'target\classes\database.properties' -Force"

echo.
echo [1/2] database.properties updated successfully!
echo [2/2] Testing connection to MySQL and initializing tables...
echo.

set "JAVA_CMD=java"
if exist "C:\Program Files\Java\jdk-26.0.1\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Java\jdk-26.0.1\bin\java.exe"
)

"%JAVA_CMD%" -cp "target/classes;lib/mysql-connector-j-8.3.0.jar;lib/*" result.database.DatabaseInitializer

echo.
echo ========================================================
echo If you saw [SUCCESS] above, you can now double-click
echo run.bat to launch the application!
echo ========================================================
echo.
pause
