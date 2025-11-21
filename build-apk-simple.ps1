# Simple script to build APK
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Building Release APK" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check for Java
Write-Host "Checking for Java..." -ForegroundColor Yellow
$javaFound = $false

# Check if java is in PATH
try {
    $null = java -version 2>&1
    $javaFound = $true
    Write-Host "Java found in PATH" -ForegroundColor Green
} catch {
    # Check common Java locations
    $javaPaths = @(
        "$env:ProgramFiles\Java\jdk-*",
        "$env:ProgramFiles\Eclipse Adoptium\jdk-*",
        "${env:ProgramFiles(x86)}\Java\jdk-*",
        "$env:LOCALAPPDATA\Programs\Android\Android Studio\jbr"
    )
    
    foreach ($pattern in $javaPaths) {
        $folders = Get-ChildItem -Path (Split-Path $pattern) -Directory -ErrorAction SilentlyContinue | Where-Object {
            $_.Name -like (Split-Path -Leaf $pattern) -or $_.Name -match "^\d+"
        }
        
        if ($folders) {
            $jdk = $folders | Sort-Object Name -Descending | Select-Object -First 1
            $javaExe = Join-Path $jdk.FullName "bin\java.exe"
            if (Test-Path $javaExe) {
                $env:JAVA_HOME = $jdk.FullName
                $env:Path = "$($jdk.FullName)\bin;$env:Path"
                $javaFound = $true
                Write-Host "Java found at: $($jdk.FullName)" -ForegroundColor Green
                break
            }
        }
    }
}

if (-not $javaFound) {
    Write-Host ""
    Write-Host "Java not found!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please install Java JDK 17 or later:" -ForegroundColor Yellow
    Write-Host "1. Go to: https://adoptium.net/" -ForegroundColor White
    Write-Host "2. Download JDK 17 for Windows x64" -ForegroundColor White
    Write-Host "3. Install it" -ForegroundColor White
    Write-Host "4. Restart PowerShell and run this script again" -ForegroundColor White
    Write-Host ""
    Write-Host "OR use Android Studio (it includes Java):" -ForegroundColor Yellow
    Write-Host "1. Install Android Studio from: https://developer.android.com/studio" -ForegroundColor White
    Write-Host "2. Open this project in Android Studio" -ForegroundColor White
    Write-Host "3. Build -> Build Bundle(s) / APK(s) -> Build APK(s)" -ForegroundColor White
    Write-Host ""
    exit 1
}

# Verify Java
Write-Host ""
Write-Host "Verifying Java..." -ForegroundColor Yellow
try {
    $version = java -version 2>&1 | Select-Object -First 1
    Write-Host "Java version: $version" -ForegroundColor Green
} catch {
    Write-Host "Java verification failed" -ForegroundColor Red
    exit 1
}

# Navigate to project
$projectPath = "C:\Users\sarang kadam\Downloads\hdck"
Set-Location $projectPath

# Check for local.properties
if (-not (Test-Path "local.properties")) {
    Write-Host ""
    Write-Host "Creating local.properties..." -ForegroundColor Yellow
    $sdkPaths = @(
        "$env:LOCALAPPDATA\Android\Sdk",
        "$env:ProgramFiles\Android\Android Studio\sdk",
        "$env:ANDROID_HOME"
    )
    
    $sdkPath = $null
    foreach ($path in $sdkPaths) {
        if (Test-Path $path) {
            $sdkPath = $path
            break
        }
    }
    
    if ($sdkPath) {
        $sdkPathEscaped = $sdkPath.Replace("\", "\\")
        "sdk.dir=$sdkPathEscaped" | Out-File -FilePath "local.properties" -Encoding ASCII
        Write-Host "Created local.properties with SDK path: $sdkPath" -ForegroundColor Green
    } else {
        Write-Host "Android SDK not found. You may need to install Android Studio." -ForegroundColor Yellow
    }
}

# Build APK
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Building Release APK..." -ForegroundColor Cyan
Write-Host "This may take 5-10 minutes..." -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

try {
    .\gradlew.bat assembleRelease --no-daemon
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "BUILD SUCCESSFUL!" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        Write-Host ""
        
        $apkPath = "app\build\outputs\apk\release\app-release-unsigned.apk"
        if (Test-Path $apkPath) {
            $apk = Get-Item $apkPath
            Write-Host "APK Location:" -ForegroundColor Cyan
            Write-Host "  $($apk.FullName)" -ForegroundColor White
            Write-Host ""
            Write-Host "Size: $([math]::Round($apk.Length / 1MB, 2)) MB" -ForegroundColor White
            Write-Host ""
            Write-Host "To install on phone:" -ForegroundColor Cyan
            Write-Host "  1. Copy this APK file to your phone" -ForegroundColor White
            Write-Host "  2. Open it on your phone" -ForegroundColor White
            Write-Host "  3. Enable 'Install from Unknown Sources' if asked" -ForegroundColor White
            Write-Host "  4. Install!" -ForegroundColor White
            Write-Host ""
            
            # Open folder
            Start-Process explorer.exe -ArgumentList "/select,`"$($apk.FullName)`""
        } else {
            Write-Host "APK not found at expected location" -ForegroundColor Yellow
            Write-Host "Check: app\build\outputs\apk\release\" -ForegroundColor White
        }
    } else {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Red
        Write-Host "BUILD FAILED" -ForegroundColor Red
        Write-Host "========================================" -ForegroundColor Red
        Write-Host ""
        Write-Host "Common issues:" -ForegroundColor Yellow
        Write-Host "1. Android SDK not installed" -ForegroundColor White
        Write-Host "2. Internet connection needed for dependencies" -ForegroundColor White
        Write-Host "3. Try: .\gradlew.bat clean" -ForegroundColor White
    }
} catch {
    Write-Host ""
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "Done!" -ForegroundColor Cyan

