# How to Build APK File

## Quick Build Instructions

### Option 1: Using Android Studio (Recommended - Easiest)

1. **Open Project in Android Studio**
   - Open Android Studio
   - Click "Open" and select the project folder: `C:\Users\sarang kadam\Downloads\hdck`

2. **Build APK**
   - Go to: `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
   - Wait for build to complete
   - Click "locate" in the notification when done
   - APK will be at: `app\build\outputs\apk\debug\app-debug.apk`

3. **Transfer to Phone**
   - Copy the APK file to your phone
   - Install it (enable "Install from Unknown Sources" if needed)

### Option 2: Using Command Line (Requires Java/JDK)

#### Step 1: Install Java JDK (if not installed)

1. Download JDK 17 or later from: https://adoptium.net/
2. Install it
3. Set JAVA_HOME environment variable:
   - Right-click "This PC" → Properties → Advanced System Settings
   - Click "Environment Variables"
   - Under "System Variables", click "New"
   - Variable name: `JAVA_HOME`
   - Variable value: `C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot` (or your JDK path)
   - Click OK

4. Add to PATH:
   - Edit "Path" variable
   - Add: `%JAVA_HOME%\bin`
   - Click OK

5. Restart terminal/PowerShell

#### Step 2: Build APK

Open PowerShell in the project folder and run:

```powershell
# Navigate to project
cd "C:\Users\sarang kadam\Downloads\hdck"

# Build debug APK
.\gradlew.bat assembleDebug

# APK will be at: app\build\outputs\apk\debug\app-debug.apk
```

#### Step 3: Build Release APK (for production)

```powershell
# Build release APK (requires signing)
.\gradlew.bat assembleRelease

# APK will be at: app\build\outputs\apk\release\app-release-unsigned.apk
```

### Option 3: Using Pre-built Script

I've created a build script. Run:

```powershell
.\build-apk.ps1
```

## APK Location After Build

- **Debug APK**: `app\build\outputs\apk\debug\app-debug.apk`
- **Release APK**: `app\build\outputs\apk\release\app-release-unsigned.apk`

## Installing on Phone

1. **Transfer APK to Phone:**
   - Use USB cable, email, cloud storage, or ADB

2. **Enable Unknown Sources:**
   - Go to Settings → Security
   - Enable "Install from Unknown Sources" or "Allow from this source"

3. **Install:**
   - Open the APK file on your phone
   - Tap "Install"
   - Wait for installation
   - Tap "Open" to launch

## Troubleshooting

### "JAVA_HOME is not set"
- Install JDK and set JAVA_HOME (see Option 2, Step 1)

### "Gradle build failed"
- Make sure you have internet connection (Gradle downloads dependencies)
- Check if Android SDK is installed
- Try: `.\gradlew.bat clean` then rebuild

### "SDK not found"
- Install Android SDK via Android Studio
- Or set ANDROID_HOME environment variable

## Quick Check Commands

```powershell
# Check Java
java -version

# Check Gradle
.\gradlew.bat --version

# Clean build
.\gradlew.bat clean

# Build debug APK
.\gradlew.bat assembleDebug
```

