@echo off
title WorkSphere Local Server
echo ================================================
echo          WorkSphere Local Server
echo ================================================
echo.
echo Installing dependencies...
npm install

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Failed to install dependencies!
    pause
    exit /b 1
)

echo.
echo Dependencies installed successfully!
echo.
echo Starting WorkSphere Local Server...
echo Server will be available at: http://localhost:3000
echo.
echo Opening browser in 3 seconds...
echo Press Ctrl+C to stop the server when done.
echo.

timeout /t 3 /nobreak > nul

echo Opening browser...
start http://localhost:3000

echo.
echo ================================================
echo   Server is starting... Browser should open!
echo   If browser doesn't open, go to:
echo   http://localhost:3000
echo ================================================
echo.

npm start

echo.
echo Server stopped. Press any key to exit...
pause > nul