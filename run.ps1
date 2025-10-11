# WorkSphere Application Launcher (PowerShell)
# This script compiles and runs the WorkSphere task management application

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "   WorkSphere Application Launcher" -ForegroundColor Cyan
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

Write-Host "Compiling and running WorkSphere..." -ForegroundColor Green
Write-Host ""

# Compile and run the application
try {
    & mvn clean compile exec:java -Dexec.mainClass="com.worksphere.WorkSphereApp"
    
    if ($LASTEXITCODE -ne 0) {
        throw "Maven execution failed"
    }
} catch {
    Write-Host ""
    Write-Host "ERROR: Failed to compile or run the application!" -ForegroundColor Red
    Write-Host "Error details: $($_.Exception.Message)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Application closed." -ForegroundColor Yellow
Read-Host "Press Enter to exit"