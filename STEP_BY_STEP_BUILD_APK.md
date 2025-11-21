# 📱 Step-by-Step: Build APK - Complete Guide

## ✅ Step 1: Install Java JDK

### Option A: Quick Install (Recommended)

1. **Download Java JDK 17:**
   - Go to: **https://adoptium.net/temurin/releases/**
   - Select:
     - **Version**: 17 (LTS)
     - **Operating System**: Windows
     - **Architecture**: x64
     - **Package Type**: JDK
   - Click **"Download"** (MSI installer)

2. **Install Java:**
   - Run the downloaded `.msi` file
   - Click **"Next"** through the installation
   - Make sure **"Add to PATH"** is checked
   - Click **"Install"**
   - Wait for installation to complete

3. **Verify Installation:**
   - Open **NEW** PowerShell window
   - Run: `java -version`
   - You should see Java version info

### Option B: Use Android Studio (Includes Java)

1. **Download Android Studio:**
   - Go to: **https://developer.android.com/studio**
   - Download and install Android Studio
   - During installation, it will install Java automatically

---

## ✅ Step 2: Set JAVA_HOME (If needed)

If Java is installed but `java -version` doesn't work:

1. **Find Java Installation:**
   - Usually at: `C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot`
   - Or: `C:\Program Files\Java\jdk-17.x.x`

2. **Set Environment Variable:**
   - Press `Win + X` → **"System"**
   - Click **"Advanced system settings"**
   - Click **"Environment Variables"**
   - Under **"System variables"**, click **"New"**
   - Variable name: `JAVA_HOME`
   - Variable value: `C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot` (your Java path)
   - Click **"OK"**

3. **Add to PATH:**
   - Find **"Path"** in System variables
   - Click **"Edit"**
   - Click **"New"**
   - Add: `%JAVA_HOME%\bin`
   - Click **"OK"** on all windows

4. **Restart PowerShell:**
   - Close all PowerShell windows
   - Open new PowerShell
   - Test: `java -version`

---

## ✅ Step 3: Build APK

### Method 1: Using Script (Easiest)

1. **Open PowerShell** in project folder:
   ```powershell
   cd "C:\Users\sarang kadam\Downloads\hdck"
   ```

2. **Run build script:**
   ```powershell
   .\build-apk-simple.ps1
   ```

3. **Wait for build** (5-10 minutes)

4. **Get APK:**
   - Location: `app\build\outputs\apk\release\app-release-unsigned.apk`

### Method 2: Manual Command

1. **Open PowerShell** in project folder:
   ```powershell
   cd "C:\Users\sarang kadam\Downloads\hdck"
   ```

2. **Build APK:**
   ```powershell
   .\gradlew.bat assembleRelease
   ```

3. **Wait for completion**

4. **Find APK:**
   - `app\build\outputs\apk\release\app-release-unsigned.apk`

### Method 3: Android Studio (No Command Line)

1. **Open Android Studio**

2. **Open Project:**
   - File → Open
   - Select: `C:\Users\sarang kadam\Downloads\hdck`

3. **Wait for Gradle Sync** (first time: 5-10 minutes)

4. **Build APK:**
   - Build → Build Bundle(s) / APK(s) → Build APK(s)

5. **Get APK:**
   - Click **"locate"** in notification
   - Or: `app\build\outputs\apk\debug\app-debug.apk`

---

## ✅ Step 4: Install APK on Phone

1. **Copy APK to Phone:**
   - USB: Connect phone, copy APK file
   - Email: Email APK to yourself, open on phone
   - Cloud: Upload to Google Drive, download on phone

2. **Enable Unknown Sources:**
   - Settings → Security → Enable "Install from Unknown Sources"
   - Or when installing, allow from that source

3. **Install:**
   - Open APK file on phone
   - Tap **"Install"**
   - Wait for installation
   - Tap **"Open"**

---

## 🆘 Troubleshooting

### "JAVA_HOME is not set"
- Install Java (Step 1)
- Set JAVA_HOME (Step 2)
- Restart PowerShell

### "SDK not found"
- Install Android Studio
- Or set `ANDROID_HOME` environment variable
- Or create `local.properties` with: `sdk.dir=C:\\Users\\YourName\\AppData\\Local\\Android\\Sdk`

### "Build failed"
- Check internet connection
- Try: `.\gradlew.bat clean` then rebuild
- Open project in Android Studio first (downloads dependencies)

### "Gradle sync failed"
- Check internet connection
- Wait longer (first time takes time)
- File → Invalidate Caches → Restart

---

## 📋 Quick Checklist

- [ ] Java JDK 17 installed
- [ ] JAVA_HOME set (if needed)
- [ ] PowerShell restarted after Java install
- [ ] `java -version` works
- [ ] In project folder: `C:\Users\sarang kadam\Downloads\hdck`
- [ ] Run: `.\gradlew.bat assembleRelease`
- [ ] APK found at: `app\build\outputs\apk\release\app-release-unsigned.apk`
- [ ] APK copied to phone
- [ ] Installed on phone

---

## 🚀 Quick Start (Copy-Paste)

```powershell
# 1. Install Java from: https://adoptium.net/
# 2. Restart PowerShell
# 3. Run these commands:

cd "C:\Users\sarang kadam\Downloads\hdck"
.\gradlew.bat assembleRelease

# 4. Find APK at: app\build\outputs\apk\release\app-release-unsigned.apk
```

---

## 📞 Need Help?

- **Java not working?** → Restart PowerShell, check JAVA_HOME
- **Build fails?** → Install Android Studio, open project once
- **APK not found?** → Check `app\build\outputs\apk\release\` folder

**Good luck!** 🎉

