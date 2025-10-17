@echo off
echo Updating WorkSphere task data...
echo.

set DB_PATH=%USERPROFILE%\.worksphere\worksphere.db
set SQL_FILE=update-task-data.sql

if not exist "%DB_PATH%" (
    echo Error: Database not found at %DB_PATH%
    echo Please run the application first to create the database.
    pause
    exit /b 1
)

echo Database: %DB_PATH%
echo SQL Script: %SQL_FILE%
echo.
echo This will replace all existing tasks with new data from Oct 15 to Nov 20, 2025
echo.
set /p CONFIRM="Continue? (Y/N): "

if /i not "%CONFIRM%"=="Y" (
    echo Operation cancelled.
    pause
    exit /b 0
)

echo.
echo Executing SQL script...
sqlite3 "%DB_PATH%" < "%SQL_FILE%"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✓ Task data updated successfully!
    echo.
    echo Updated task distribution:
    echo - Date range: October 15 to November 20, 2025
    echo - Weekdays only (no weekends)
    echo - 2 completed tasks per user per weekday
    echo - Total: ~76 completed tasks
    echo.
    echo Please restart WorkSphere to see the updated leaderboard data.
) else (
    echo.
    echo ✗ Error updating task data.
    echo Please check if SQLite3 is installed and in your PATH.
)

echo.
pause
