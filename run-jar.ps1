# WorkSphere Application Builder and Launcher (PowerShell)
# Creates a standalone JAR file and runs the application

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "   WorkSphere JAR Builder" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""

# Check if Maven is available
try {
    $null = & mvn --version 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "Maven not found"
    }
} catch {
    Write-Host "ERROR: Maven is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Maven and add it to your PATH" -ForegroundColor Yellow
    Write-Host "Download from: https://maven.apache.org/download.cgi" -ForegroundColor Yellow
    Write-Host ""
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "Building standalone JAR file..." -ForegroundColor Green
Write-Host ""

# Clean and package the application
try {
    & mvn clean package -DskipTests
    
    if ($LASTEXITCODE -ne 0) {
        throw "Build failed"
    }
} catch {
    Write-Host ""
    Write-Host "ERROR: Build failed!" -ForegroundColor Red
    Write-Host "Error details: $($_.Exception.Message)" -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""
Write-Host "Build successful! Running WorkSphere..." -ForegroundColor Green
Write-Host ""

# Check if JAR file exists
$jarPath = "target\worksphere-1.0.0.jar"
if (-not (Test-Path $jarPath)) {
    Write-Host "ERROR: JAR file not found at $jarPath" -ForegroundColor Red
    Write-Host "Build may have failed or JAR name is different." -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}

# Run the standalone JAR
try {
    & java -jar $jarPath
    
    if ($LASTEXITCODE -ne 0) {
        throw "Java execution failed"
    }
} catch {
    Write-Host ""
    Write-Host "Note: If you get a 'no main manifest attribute' error," -ForegroundColor Yellow
    Write-Host "the JAR might not have been built correctly." -ForegroundColor Yellow
    Write-Host "Try running: mvn clean package" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Error details: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "Application closed." -ForegroundColor Yellow
Read-Host "Press Enter to exit"