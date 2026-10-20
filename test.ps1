Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "Running Automated Test Suite..." -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan

java -cp "bin;lib/*" com.college.eventmanager.AppTestSuite
