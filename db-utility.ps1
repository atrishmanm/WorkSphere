# WorkSphere Database Utility Script (PowerShell)
# This script provides database management functions

param(
    [Parameter(Position=0)]
    [string]$Command
)

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "   WorkSphere Database Utility" -ForegroundColor Cyan
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
    Read-Host "Press Enter to exit"
    exit 1
}

if (-not $Command) {
    Write-Host "Available commands:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "  reset-task-numbers  - Reset task numbering to start from 1" -ForegroundColor White
    Write-Host "  show-task-info      - Show next task ID that will be assigned" -ForegroundColor White
    Write-Host "  test-connection     - Test database connection" -ForegroundColor White
    Write-Host ""
    Write-Host "Usage: .\db-utility.ps1 [command]" -ForegroundColor Green
    Write-Host ""
    Read-Host "Press Enter to exit"
    exit 0
}

Write-Host "Compiling project..." -ForegroundColor Green
try {
    & mvn -q compile
    
    if ($LASTEXITCODE -ne 0) {
        throw "Compilation failed"
    }
} catch {
    Write-Host "ERROR: Project compilation failed" -ForegroundColor Red
    Write-Host "Error details: $($_.Exception.Message)" -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""
Write-Host "Running database utility: $Command" -ForegroundColor Green
Write-Host ""

# Run the database utility with the specified command
try {
    & mvn -q exec:java -Dexec.mainClass="com.worksphere.util.DatabaseUtility" -Dexec.args="$Command"
    
    if ($LASTEXITCODE -ne 0) {
        throw "Database utility execution failed"
    }
} catch {
    Write-Host "ERROR: Database utility failed" -ForegroundColor Red
    Write-Host "Error details: $($_.Exception.Message)" -ForegroundColor Yellow
}

Write-Host ""
Read-Host "Press Enter to exit"