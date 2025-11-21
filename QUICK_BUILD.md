# Quick APK Build Guide

## ⚡ Fastest Method: Android Studio

1. **Open Android Studio**
2. **File → Open** → Select `hdck` folder
3. **Wait for Gradle sync** (first time may take a few minutes)
4. **Build → Build Bundle(s) / APK(s) → Build APK(s)**
5. **APK Location**: `app\build\outputs\apk\debug\app-debug.apk`

**That's it!** The APK will be ready in a few minutes.

## 🔧 If Using Command Line

### Step 1: Set Android SDK Path

Find your Android SDK location:
- Usually: `C:\Users\YourName\AppData\Local\Android\Sdk`
- Or check Android Studio: File → Settings → Android SDK

Update `local.properties`:
```properties
sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
```

### Step 2: Build

```powershell
cd "C:\Users\sarang kadam\Downloads\hdck"
.\build-apk.ps1
```

Or manually:
```powershell
.\gradlew.bat assembleDebug
```

### Step 3: Find APK

```
app\build\outputs\apk\debug\app-debug.apk
```

## 📱 Install APK

```powershell
# Connect device via USB
# Enable USB debugging
adb install app\build\outputs\apk\debug\app-debug.apk
```

## ⚠️ Common Issues

**"SDK location not found"**
→ Update `local.properties` with correct SDK path

**"Gradle sync failed"**
→ Open in Android Studio first, let it sync

**"Build failed"**
→ Check internet connection (needed for dependencies)

## ✅ Recommended: Use Android Studio

Android Studio handles everything automatically:
- Downloads Gradle wrapper
- Configures SDK
- Downloads dependencies
- Builds APK

Just open the project and click Build!

