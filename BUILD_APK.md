# Build APK - Quick Guide

## Method 1: Using Android Studio (Recommended)

1. **Open Project**
   - Open Android Studio
   - File → Open → Select `hdck` folder
   - Wait for Gradle sync

2. **Configure SDK** (if needed)
   - File → Settings → Appearance & Behavior → System Settings → Android SDK
   - Note the SDK path
   - Update `local.properties` with: `sdk.dir=C\:\\path\\to\\Android\\Sdk`

3. **Build APK**
   - Build → Build Bundle(s) / APK(s) → Build APK(s)
   - Or: Build → Generate Signed Bundle / APK → APK

4. **Find APK**
   - Location: `app\build\outputs\apk\debug\app-debug.apk`

## Method 2: Using Command Line

### Prerequisites
- Android SDK installed
- Java JDK 8+ installed
- Gradle (or use wrapper)

### Steps

1. **Set Android SDK Path**
   ```powershell
   # Find your Android SDK path
   # Usually: C:\Users\YourName\AppData\Local\Android\Sdk
   
   # Update local.properties
   # sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
   ```

2. **Download Gradle Wrapper** (if needed)
   ```powershell
   # Gradle will download automatically on first run
   # Or download from: https://gradle.org/releases/
   ```

3. **Build APK**
   ```powershell
   cd "C:\Users\sarang kadam\Downloads\hdck"
   
   # Build debug APK
   .\gradlew.bat assembleDebug
   
   # Or build release APK (requires signing)
   .\gradlew.bat assembleRelease
   ```

4. **Find APK**
   - Debug: `app\build\outputs\apk\debug\app-debug.apk`
   - Release: `app\build\outputs\apk\release\app-release.apk`

## Method 3: Using Android Studio Terminal

1. Open Android Studio
2. Open Terminal (View → Tool Windows → Terminal)
3. Run:
   ```bash
   ./gradlew assembleDebug
   ```
4. APK will be in `app/build/outputs/apk/debug/`

## Troubleshooting

### Error: "SDK location not found"
- Create/update `local.properties` with correct SDK path
- Format: `sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk`

### Error: "Gradle wrapper not found"
- Android Studio will download it automatically
- Or run: `gradle wrapper` (if Gradle is installed)

### Error: "Build failed"
- Check internet connection (Gradle downloads dependencies)
- Sync Gradle: File → Sync Project with Gradle Files
- Clean project: Build → Clean Project
- Rebuild: Build → Rebuild Project

### Error: "Package not found"
- Sync Gradle files
- Check `build.gradle` dependencies
- Verify internet connection

## APK Locations

After successful build:
- **Debug APK**: `app\build\outputs\apk\debug\app-debug.apk`
- **Release APK**: `app\build\outputs\apk\release\app-release.apk` (if signed)

## Install APK

```powershell
# Connect Android device via USB
# Enable USB debugging
adb install app\build\outputs\apk\debug\app-debug.apk
```

## Quick Build Script

Save as `build-apk.ps1`:
```powershell
cd "C:\Users\sarang kadam\Downloads\hdck"
Write-Host "Building APK..."
.\gradlew.bat assembleDebug
if ($LASTEXITCODE -eq 0) {
    Write-Host "APK built successfully!"
    Write-Host "Location: app\build\outputs\apk\debug\app-debug.apk"
} else {
    Write-Host "Build failed. Check errors above."
}
```

