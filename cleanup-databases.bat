@echo off
REM Clean up old database files that might be causing duplicates
echo ====================================
echo   WorkSphere Database Cleanup
echo ====================================
echo.

echo Looking for old database files...
echo.

REM Check for database files in current directory
if exist "worksphere.db" (
    echo Found old database in current directory: worksphere.db
    echo Deleting old database file...
    del "worksphere.db"
    echo ✅ Deleted worksphere.db from current directory
    echo.
)

REM Check for database files in project subdirectories
for /r . %%i in (*.db) do (
    if not "%%i"=="%USERPROFILE%\.worksphere\worksphere.db" (
        echo Found database file: %%i
        echo Deleting: %%i
        del "%%i"
        echo ✅ Deleted: %%i
        echo.
    )
)

echo Current database location should be:
echo %USERPROFILE%\.worksphere\worksphere.db
echo.

if exist "%USERPROFILE%\.worksphere\worksphere.db" (
    echo ✅ Main database exists and is preserved.
) else (
    echo ℹ️  Main database doesn't exist yet - it will be created on first run.
)

echo.
echo Cleanup completed. All execution methods should now use the same database.
pause