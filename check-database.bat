@echo off
REM Check database status and task count
echo ====================================
echo     WorkSphere Database Checker
echo ====================================
echo.

REM Check if database file exists
if exist "%USERPROFILE%\.worksphere\worksphere.db" (
    echo ✅ Database found at: %USERPROFILE%\.worksphere\worksphere.db
    echo.
    
    REM Use SQLite to check task count
    echo Task count check:
    sqlite3 "%USERPROFILE%\.worksphere\worksphere.db" "SELECT COUNT(*) as 'Total Tasks' FROM tasks;"
    echo.
    
    echo Sample task check:
    sqlite3 "%USERPROFILE%\.worksphere\worksphere.db" "SELECT COUNT(*) as 'Sample Tasks' FROM tasks WHERE title LIKE 'Setup project environment' OR title LIKE 'Design database schema';"
    echo.
    
    echo Recent tasks:
    sqlite3 "%USERPROFILE%\.worksphere\worksphere.db" "SELECT id, title, status FROM tasks ORDER BY id DESC LIMIT 5;"
    
) else (
    echo ❌ Database not found at: %USERPROFILE%\.worksphere\worksphere.db
    echo The database will be created when you first run the application.
)

echo.
echo Check completed.
pause