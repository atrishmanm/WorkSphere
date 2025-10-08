# WorkSphere Local Server Startup Script
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "          WorkSphere Local Server" -ForegroundColor Cyan  
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Installing dependencies..." -ForegroundColor Yellow
npm install

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to install dependencies!" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""
Write-Host "Dependencies installed successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "Starting WorkSphere Local Server..." -ForegroundColor Yellow
Write-Host "Server will be available at: http://localhost:3000" -ForegroundColor Green
Write-Host ""
Write-Host "Opening browser in 3 seconds..." -ForegroundColor Yellow
Write-Host "Press Ctrl+C to stop the server when done." -ForegroundColor Gray
Write-Host ""

Start-Sleep -Seconds 3

Write-Host "Opening browser..." -ForegroundColor Yellow
Start-Process "http://localhost:3000"

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "   Server is starting... Browser should open!" -ForegroundColor Green
Write-Host "   If browser doesn't open, go to:" -ForegroundColor Yellow
Write-Host "   http://localhost:3000" -ForegroundColor White
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

npm start

Write-Host ""
Write-Host "Server stopped. Press any key to exit..." -ForegroundColor Yellow
Read-Host