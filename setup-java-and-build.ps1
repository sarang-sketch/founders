# Script to Install Java and Build APK
# Run this script as Administrator for best results

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Java Setup and APK Build Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if running as Administrator
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "WARNING: Not running as Administrator" -ForegroundColor Yellow
    Write-Host "Some steps may require admin privileges" -ForegroundColor Yellow
    Write-Host ""
}

# Step 1: Check if Java is already installed
Write-Host "Step 1: Checking for existing Java installation..." -ForegroundColor Green
$javaInstalled = $false
$javaPath = $null

# Check common Java locations
$javaLocations = @(
    "$env:ProgramFiles\Java",
    "${env:ProgramFiles(x86)}\Java",
    "$env:ProgramFiles\Eclipse Adoptium",
    "$env:LOCALAPPDATA\Programs\Android\Android Studio\jbr",
    "$env:ProgramFiles\Android\Android Studio\jbr"
)

foreach ($location in $javaLocations) {
    if (Test-Path $location) {
        $jdkFolders = Get-ChildItem $location -Directory -ErrorAction SilentlyContinue | Where-Object {
            $_.Name -like "*jdk*" -or $_.Name -like "*jbr*" -or $_.Name -match "^\d+"
        }
        
        if ($jdkFolders) {
            $latestJdk = $jdkFolders | Sort-Object Name -Descending | Select-Object -First 1
            $javaExe = Join-Path $latestJdk.FullName "bin\java.exe"
            if (Test-Path $javaExe) {
                $javaPath = $latestJdk.FullName
                Write-Host "  Found Java at: $javaPath" -ForegroundColor Green
                $javaInstalled = $true
                break
            }
        }
    }
}

# Check PATH
if (-not $javaInstalled) {
    try {
        $javaVersion = java -version 2>&1 | Select-Object -First 1
        if ($javaVersion) {
            Write-Host "  Java found in PATH" -ForegroundColor Green
            $javaInstalled = $true
        }
    } catch {
        # Java not in PATH
    }
}

if (-not $javaInstalled) {
    Write-Host "  Java not found" -ForegroundColor Red
    Write-Host ""
    Write-Host "Step 2: Installing Java JDK..." -ForegroundColor Green
    Write-Host ""
    Write-Host "OPTION 1: Automatic Download (Recommended)" -ForegroundColor Yellow
    Write-Host "  I'll download and install Java JDK 17 for you" -ForegroundColor White
    Write-Host ""
    
    $installJava = Read-Host "Do you want to download Java JDK 17? (Y/N)"
    
    if ($installJava -eq "Y" -or $installJava -eq "y") {
        Write-Host ""
        Write-Host "Downloading Java JDK 17..." -ForegroundColor Cyan
        
        # Download Java JDK 17 from Adoptium
        $downloadUrl = "https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse"
        $tempDir = $env:TEMP
        $zipFile = Join-Path $tempDir "jdk17.zip"
        $installDir = "$env:ProgramFiles\Eclipse Adoptium"
        
        try {
            Write-Host "  Downloading from Adoptium..." -ForegroundColor White
            Invoke-WebRequest -Uri $downloadUrl -OutFile $zipFile -UseBasicParsing
            
            Write-Host "  Extracting..." -ForegroundColor White
            if (-not (Test-Path $installDir)) {
                New-Item -ItemType Directory -Path $installDir -Force | Out-Null
            }
            
            Expand-Archive -Path $zipFile -DestinationPath $installDir -Force
            
            # Find the JDK folder
            $jdkFolder = Get-ChildItem $installDir -Directory | Where-Object { $_.Name -like "*jdk*" } | Select-Object -First 1
            if ($jdkFolder) {
                $javaPath = $jdkFolder.FullName
                Write-Host "  Java installed at: $javaPath" -ForegroundColor Green
                
                # Set JAVA_HOME
                Write-Host "  Setting JAVA_HOME..." -ForegroundColor White
                [Environment]::SetEnvironmentVariable("JAVA_HOME", $javaPath, "Machine")
                $env:JAVA_HOME = $javaPath
                
                # Add to PATH
                $javaBin = Join-Path $javaPath "bin"
                $currentPath = [Environment]::GetEnvironmentVariable("Path", "Machine")
                if ($currentPath -notlike "*$javaBin*") {
                    $newPath = "$currentPath;$javaBin"
                    [Environment]::SetEnvironmentVariable("Path", $newPath, "Machine")
                    $env:Path += ";$javaBin"
                }
                
                Write-Host "  JAVA_HOME set to: $javaPath" -ForegroundColor Green
                $javaInstalled = $true
            }
            
            # Cleanup
            Remove-Item $zipFile -ErrorAction SilentlyContinue
        } catch {
            Write-Host "  Automatic installation failed: $($_.Exception.Message)" -ForegroundColor Red
            Write-Host ""
            Write-Host "OPTION 2: Manual Installation" -ForegroundColor Yellow
            Write-Host "  1. Go to: https://adoptium.net/" -ForegroundColor White
            Write-Host "  2. Download JDK 17 for Windows x64" -ForegroundColor White
            Write-Host "  3. Install it" -ForegroundColor White
            Write-Host "  4. Set JAVA_HOME to installation path" -ForegroundColor White
            Write-Host "  5. Add %JAVA_HOME%\bin to PATH" -ForegroundColor White
            Write-Host "  6. Restart PowerShell and run this script again" -ForegroundColor White
            Write-Host ""
            exit 1
        }
    } else {
        Write-Host ""
        Write-Host "Please install Java manually:" -ForegroundColor Yellow
        Write-Host "  1. Download from: https://adoptium.net/" -ForegroundColor White
        Write-Host "  2. Install JDK 17" -ForegroundColor White
        Write-Host "  3. Set JAVA_HOME environment variable" -ForegroundColor White
        Write-Host "  4. Run this script again" -ForegroundColor White
        Write-Host ""
        exit 1
    }
} else {
    # Set JAVA_HOME if we found Java
    if ($javaPath) {
        $env:JAVA_HOME = $javaPath
        [Environment]::SetEnvironmentVariable("JAVA_HOME", $javaPath, "User")
                Write-Host "  JAVA_HOME set to: $javaPath" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "Step 3: Verifying Java installation..." -ForegroundColor Green
try {
    $javaVersion = & "$env:JAVA_HOME\bin\java.exe" -version 2>&1
    if ($javaVersion) {
        Write-Host "  Java is working!" -ForegroundColor Green
        $javaVersion | Select-Object -First 1 | ForEach-Object { Write-Host "  $_" -ForegroundColor White }
    }
} catch {
    Write-Host "  ✗ Java verification failed" -ForegroundColor Red
    Write-Host "  Please restart PowerShell and try again" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "Step 4: Building Release APK..." -ForegroundColor Green
Write-Host ""

# Navigate to project directory
$projectPath = "C:\Users\sarang kadam\Downloads\hdck"
Set-Location $projectPath

# Check if local.properties exists
if (-not (Test-Path "local.properties")) {
    Write-Host "  Creating local.properties..." -ForegroundColor Yellow
    $sdkPath = "$env:LOCALAPPDATA\Android\Sdk"
    if (-not (Test-Path $sdkPath)) {
        $sdkPath = "$env:ProgramFiles\Android\Android Studio\sdk"
    }
    if (Test-Path $sdkPath) {
        $sdkPath = $sdkPath.Replace("\", "\\")
        "sdk.dir=$sdkPath" | Out-File -FilePath "local.properties" -Encoding ASCII
        Write-Host "  ✓ local.properties created" -ForegroundColor Green
    } else {
        Write-Host "  ⚠ Android SDK not found. You may need to install Android Studio" -ForegroundColor Yellow
    }
}

# Build release APK
Write-Host "  Running: .\gradlew.bat assembleRelease" -ForegroundColor Cyan
Write-Host "  This may take 5-10 minutes on first build..." -ForegroundColor Yellow
Write-Host ""

try {
    $buildOutput = .\gradlew.bat assembleRelease --no-daemon 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "✓ BUILD SUCCESSFUL!" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        Write-Host ""
        
        $apkPath = "app\build\outputs\apk\release\app-release-unsigned.apk"
        if (Test-Path $apkPath) {
            $apkInfo = Get-Item $apkPath
            Write-Host "APK Location:" -ForegroundColor Cyan
            Write-Host "  $($apkInfo.FullName)" -ForegroundColor White
            Write-Host ""
            Write-Host "APK Size: $([math]::Round($apkInfo.Length / 1MB, 2)) MB" -ForegroundColor White
            Write-Host "Created: $($apkInfo.LastWriteTime)" -ForegroundColor White
            Write-Host ""
            Write-Host "To install on phone:" -ForegroundColor Cyan
            Write-Host "  1. Copy APK to your phone" -ForegroundColor White
            Write-Host "  2. Open APK file on phone" -ForegroundColor White
            Write-Host "  3. Enable 'Install from Unknown Sources' if asked" -ForegroundColor White
            Write-Host "  4. Install and enjoy!" -ForegroundColor White
            Write-Host ""
            
            # Open folder
            $openFolder = Read-Host "Open APK folder? (Y/N)"
            if ($openFolder -eq "Y" -or $openFolder -eq "y") {
                Start-Process explorer.exe -ArgumentList "/select,`"$($apkInfo.FullName)`""
            }
        } else {
            Write-Host "  ⚠ APK file not found at expected location" -ForegroundColor Yellow
            Write-Host "  Check: app\build\outputs\apk\release\" -ForegroundColor White
        }
    } else {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Red
        Write-Host "✗ BUILD FAILED" -ForegroundColor Red
        Write-Host "========================================" -ForegroundColor Red
        Write-Host ""
        Write-Host "Error output:" -ForegroundColor Yellow
        $buildOutput | Select-Object -Last 20 | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
        Write-Host ""
        Write-Host "Common fixes:" -ForegroundColor Yellow
        Write-Host "  1. Make sure Android SDK is installed" -ForegroundColor White
        Write-Host "  2. Check internet connection (dependencies download)" -ForegroundColor White
        Write-Host "  3. Try: .\gradlew.bat clean" -ForegroundColor White
        Write-Host "  4. Open project in Android Studio first" -ForegroundColor White
    }
} catch {
    Write-Host ""
    Write-Host "✗ Build error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please check:" -ForegroundColor Yellow
    Write-Host "  1. Java is installed and JAVA_HOME is set" -ForegroundColor White
    Write-Host "  2. Android SDK is installed" -ForegroundColor White
    Write-Host "  3. Internet connection is available" -ForegroundColor White
}

Write-Host ""
Write-Host "Script completed!" -ForegroundColor Cyan

