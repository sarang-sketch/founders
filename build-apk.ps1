# Build APK Script for MGM Lost & Found App
# Run this script to build the debug APK

Write-Host "========================================"
Write-Host "Building MGM Lost & Found APK"
Write-Host "========================================"
Write-Host ""

# Change to project directory
$projectPath = "C:\Users\sarang kadam\Downloads\hdck"
Set-Location $projectPath

# Check if local.properties exists
if (-not (Test-Path "local.properties")) {
    Write-Host "WARNING: local.properties not found!" -ForegroundColor Yellow
    Write-Host "Please set your Android SDK path in local.properties" -ForegroundColor Yellow
    Write-Host "Format: sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk" -ForegroundColor Yellow
    Write-Host ""
}

# Check if gradlew.bat exists
if (-not (Test-Path "gradlew.bat")) {
    Write-Host "ERROR: gradlew.bat not found!" -ForegroundColor Red
    Write-Host "Please open project in Android Studio first to generate Gradle wrapper" -ForegroundColor Red
    exit 1
}

Write-Host "Starting build process..." -ForegroundColor Green
Write-Host ""

# Build debug APK
try {
    .\gradlew.bat assembleDebug --no-daemon
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "========================================"
        Write-Host "BUILD SUCCESSFUL!" -ForegroundColor Green
        Write-Host "========================================"
        Write-Host ""
        Write-Host "APK Location:" -ForegroundColor Cyan
        Write-Host "  app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor White
        Write-Host ""
        Write-Host "To install on device:" -ForegroundColor Cyan
        Write-Host "  adb install app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor White
        Write-Host ""
    } else {
        Write-Host ""
        Write-Host "========================================"
        Write-Host "BUILD FAILED!" -ForegroundColor Red
        Write-Host "========================================"
        Write-Host ""
        Write-Host "Please check the errors above." -ForegroundColor Yellow
        Write-Host "Common issues:" -ForegroundColor Yellow
        Write-Host "  1. Android SDK not configured (check local.properties)" -ForegroundColor Yellow
        Write-Host "  2. Internet connection needed for dependencies" -ForegroundColor Yellow
        Write-Host "  3. Open project in Android Studio first" -ForegroundColor Yellow
        Write-Host ""
        exit 1
    }
} catch {
    Write-Host ""
    Write-Host "ERROR: Build failed with exception" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    Write-Host ""
    Write-Host "Recommendation: Open project in Android Studio and build from there" -ForegroundColor Yellow
    exit 1
}

