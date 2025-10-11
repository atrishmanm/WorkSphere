@echo off
REM WorkSphere Database Utility Script
REM This script provides database management functions

echo ====================================
echo    WorkSphere Database Utility
echo ====================================
echo.

REM Check if Maven is available
mvn --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven and add it to your PATH
    pause
    exit /b 1
)

if "%1"=="" (
    echo Available commands:
    echo.
    echo   reset-task-numbers  - Reset task numbering to start from 1
    echo   show-task-info      - Show next task ID that will be assigned
    echo   test-connection     - Test database connection
    echo.
    echo Usage: db-utility.bat [command]
    echo.
    pause
    exit /b 0
)

echo Compiling project...
mvn -q compile

if %errorlevel% neq 0 (
    echo ERROR: Project compilation failed
    pause
    exit /b 1
)

echo.
echo Running database utility: %1
echo.

REM Run the database utility with the specified command
mvn -q exec:java -Dexec.mainClass="com.worksphere.util.DatabaseUtility" -Dexec.args="%1"

echo.
pause