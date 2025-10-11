@echo off
REM WorkSphere Application Launcher
REM This script compiles and runs the WorkSphere task management application

echo ====================================
echo     WorkSphere Application Launcher
echo ====================================
echo.

REM Check if Maven is available
mvn --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven and add it to your PATH
    echo Download from: https://maven.apache.org/download.cgi
    echo.
    pause
    exit /b 1
)

echo Compiling and running WorkSphere...
echo.

REM Compile and run the application
mvn clean compile exec:java -Dexec.mainClass="com.worksphere.WorkSphereApp"

echo.
echo Application closed.
pause