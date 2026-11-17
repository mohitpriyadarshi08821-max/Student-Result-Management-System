@echo off
setlocal
echo ====================================================================
echo   Student Result Management System - Web Browser Launcher
echo ====================================================================

node -v >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Node.js is required to host the CheerpJ Web Preview server.
    echo Please install Node.js from https://nodejs.org/ or use run.bat for desktop mode.
    pause
    exit /b 1
)

echo Starting local WebAssembly server and opening your web browser...
start "" "http://localhost:8080"
node web/server.js
pause
