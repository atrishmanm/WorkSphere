@echo off
REM WorkSphere Application Builder and Launcher
REM Creates a standalone JAR file and runs the application

echo ====================================
echo     WorkSphere JAR Builder
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

echo Building standalone JAR file...
echo.

REM Clean and package the application
mvn clean package -DskipTests

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo Build successful! Running WorkSphere...
echo.

REM Check if JAR file exists
if not exist target\worksphere-1.0.0.jar (
    echo ERROR: JAR file not found at target\worksphere-1.0.0.jar
    echo Build may have failed or JAR name is different.
    pause
    exit /b 1
)


REM Run the standalone JAR in GUI mode (no output redirection)
echo Starting Java application...
call java -jar target\worksphere-1.0.0.jar gui
echo Java process exited with code %errorlevel%.

if %errorlevel% neq 0 (
    echo.
    echo Note: If you get a "no main manifest attribute" error,
    echo the JAR might not have been built correctly.
    echo Try running: mvn clean package
    echo.
)

echo.
echo Application closed. See run-jar.log for details.
pause