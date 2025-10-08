@echo off
title WorkSphere Task Management System
color 0A
echo.
echo ========================================
echo    WorkSphere Task Management System
echo ========================================
echo.

REM Check if Node.js is installed
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Node.js is not installed!
    echo.
    echo Please install Node.js from: https://nodejs.org/
    echo Choose the LTS version and run this script again.
    echo.
    pause
    exit /b 1
)

echo [✓] Node.js detected: 
node --version

REM Create package.json if it doesn't exist
if not exist package.json (
    echo [INFO] Creating package.json...
    echo {"name":"worksphere","version":"1.0.0","main":"server.js","scripts":{"start":"node server.js"},"dependencies":{"express":"^4.18.0","cors":"^2.8.5"}} > package.json
)

REM Install dependencies
echo [INFO] Installing/checking dependencies...
npm install express cors >nul 2>&1

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Failed to install dependencies!
    echo Please check your internet connection and try again.
    pause
    exit /b 1
)

echo [✓] Dependencies ready!

REM Kill any existing servers on port 3000
echo [INFO] Checking for existing servers...
for /f "tokens=5" %%a in ('netstat -ano 2^>nul ^| findstr :3000') do (
    echo [INFO] Stopping existing server...
    taskkill /PID %%a /F >nul 2>&1
)

echo.
echo ========================================
echo   🚀 Starting WorkSphere Server...
echo ========================================
echo   📍 URL: http://localhost:3000
echo   👤 Admin Login: admin / admin  
echo   👤 User Login:  user / user123
echo ========================================
echo.
echo [INFO] Opening browser in 3 seconds...
echo [INFO] Press Ctrl+C to stop server when done
echo.

timeout /t 3 /nobreak > nul

REM Open browser
start http://localhost:3000

REM Start server
node server.js

echo.
echo [INFO] Server stopped. Press any key to exit...
pause > nul