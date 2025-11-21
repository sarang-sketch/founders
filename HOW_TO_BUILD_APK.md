# 📱 How to Build APK File - Simple Guide

## 🎯 Easiest Method: Android Studio (Recommended)

### Step 1: Open Project in Android Studio

1. **Download Android Studio** (if not installed):
   - Go to: https://developer.android.com/studio
   - Download and install Android Studio

2. **Open the Project**:
   - Launch Android Studio
   - Click **"Open"** or **"File" → "Open"**
   - Navigate to: `C:\Users\sarang kadam\Downloads\hdck`
   - Click **"OK"**
   - Wait for Gradle sync to complete (first time may take 5-10 minutes)

### Step 2: Build APK

1. **Build Menu**:
   - Click **"Build"** in the top menu
   - Select **"Build Bundle(s) / APK(s)"**
   - Click **"Build APK(s)"**

2. **Wait for Build**:
   - Build will start (check bottom status bar)
   - Wait for "Build completed successfully" message

3. **Locate APK**:
   - When build completes, click **"locate"** in the notification
   - Or navigate to: `app\build\outputs\apk\debug\app-debug.apk`

### Step 3: Install on Your Phone

**Method 1: USB Transfer**
1. Connect phone to computer via USB
2. Copy `app-debug.apk` to your phone
3. On phone: Open file manager → Find APK → Tap to install
4. Enable "Install from Unknown Sources" if prompted

**Method 2: Email/Cloud**
1. Email the APK to yourself or upload to Google Drive
2. Open email/Drive on phone
3. Download APK
4. Tap to install

**Method 3: ADB (Advanced)**
```bash
adb install app\build\outputs\apk\debug\app-debug.apk
```

---

## 🔧 Alternative: Command Line Build

### Prerequisites

1. **Install Java JDK 17+**:
   - Download from: https://adoptium.net/
   - Install JDK
   - Set JAVA_HOME environment variable:
     - Windows: System Properties → Environment Variables
     - Add: `JAVA_HOME = C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot`
     - Add to PATH: `%JAVA_HOME%\bin`

2. **Restart PowerShell/Terminal**

### Build Commands

Open PowerShell in project folder:

```powershell
# Navigate to project
cd "C:\Users\sarang kadam\Downloads\hdck"

# Build debug APK
.\gradlew.bat assembleDebug

# APK location: app\build\outputs\apk\debug\app-debug.apk
```

---

## 📍 APK File Location

After successful build, APK will be at:

```
app\build\outputs\apk\debug\app-debug.apk
```

---

## ✅ Quick Checklist

- [ ] Android Studio installed (or Java JDK installed)
- [ ] Project opened in Android Studio
- [ ] Gradle sync completed
- [ ] Build → Build APK(s) clicked
- [ ] APK file found in `app\build\outputs\apk\debug\`
- [ ] APK transferred to phone
- [ ] Installed on phone

---

## 🆘 Troubleshooting

### "Gradle sync failed"
- Check internet connection (Gradle downloads dependencies)
- Wait for sync to complete (first time takes longer)
- Try: **File → Invalidate Caches → Restart**

### "SDK not found"
- Android Studio will prompt to install SDK
- Click "Install" when prompted
- Or: **Tools → SDK Manager → Install required SDKs**

### "Build failed"
- Check error message in "Build" tab at bottom
- Common fix: **Build → Clean Project**, then rebuild

### "Cannot find JAVA_HOME"
- Install JDK (see above)
- Set JAVA_HOME environment variable
- Restart Android Studio/PowerShell

---

## 📝 Notes

- **Debug APK**: For testing (larger file size, not optimized)
- **Release APK**: For production (requires signing, smaller size)
- First build takes longer (downloads dependencies)
- Make sure you have internet connection for first build

---

## 🚀 Quick Start (Copy-Paste)

If you have Android Studio:

1. Open Android Studio
2. File → Open → Select project folder
3. Build → Build Bundle(s) / APK(s) → Build APK(s)
4. Click "locate" when done
5. Copy APK to phone and install

**That's it!** 🎉
