# Build and Test Guide - MGM Lost & Found App

## 🚀 Quick Start

### Prerequisites
- ✅ Android Studio installed
- ✅ JDK 8 or higher
- ✅ Android SDK (API 24+)
- ✅ Firebase project (needs setup)
- ✅ Supabase database (✅ Already configured via MCP)

## 📦 Build the App

### Option 1: Using Android Studio
1. Open Android Studio
2. File → Open → Select `hdck` folder
3. Wait for Gradle sync
4. Build → Make Project (Ctrl+F9)
5. Run → Run 'app' (Shift+F10)

### Option 2: Using Command Line
```bash
# Windows PowerShell
cd "C:\Users\sarang kadam\Downloads\hdck"

# Build debug APK
.\gradlew.bat assembleDebug

# Install on connected device
.\gradlew.bat installDebug

# Or use ADB
adb install app\build\outputs\apk\debug\app-debug.apk
```

## 🧪 Testing Checklist

### 1. Database Connection Test ✅
**Status**: Already tested via MCP
- ✅ Supabase tables created
- ✅ Real-time enabled
- ✅ RLS policies configured

**Verify**:
```sql
-- Run in Supabase SQL Editor
SELECT * FROM users LIMIT 1;
SELECT * FROM items LIMIT 1;
SELECT * FROM claim_requests LIMIT 1;
```

### 2. Authentication Test
**Steps**:
1. Launch app
2. Tap "Demo Login"
3. Should navigate to MainActivity
4. Check Supabase `users` table for demo user

**Expected**: User logged in, can see main screen

### 3. Registration Test
**Steps**:
1. Tap "Sign in with Google"
2. Select Google account
3. Fill registration form:
   - Student ID: TEST001
   - Phone: 1234567890
   - Department: Computer Science
   - Year: 3rd Year
4. Submit

**Expected**: 
- User created in Supabase `users` table
- Navigate to MainActivity

### 4. Report Lost Item Test
**Steps**:
1. Navigate to "Lost Items" tab
2. Tap FAB (+ button)
3. Fill form:
   - Title: "Lost iPhone"
   - Category: Phone
   - Description: "Black iPhone 13 with case"
   - Location: "Library Building"
4. Tap "Get Current Location" (grant permission)
5. Tap "Take Photo" (grant permission)
6. Submit

**Expected**:
- Item appears in Lost Items list
- Item saved in Supabase `items` table
- `type` = 'LOST'
- `status` = 'ACTIVE'

### 5. Report Found Item Test
**Steps**:
1. Navigate to "Found Items" tab
2. Tap FAB
3. Fill form:
   - Title: "Found Phone"
   - Category: Phone
   - Description: "Black phone found"
4. Submit

**Expected**:
- Item appears in Found Items list
- Item saved in Supabase

### 6. Real-time Test
**Steps**:
1. Open app on Device A
2. Open app on Device B (or emulator)
3. On Device A, report a new item
4. On Device B, verify item appears without refresh

**Expected**: Item appears automatically on Device B

### 7. Matching Test
**Steps**:
1. Report lost item: "iPhone 13, black case" (Category: PHONE)
2. Report found item: "Black iPhone found" (Category: PHONE)
3. Wait a few seconds
4. Check Supabase `items` table
5. Verify `matched_items` array contains item IDs

**Expected**:
- Matching algorithm triggers
- `matched_items` array populated
- Notification received (if FCM configured)

### 8. Claim Request Test
**Steps**:
1. View a found item
2. Tap "Claim" button
3. Submit claim request
4. Check Supabase `claim_requests` table

**Expected**:
- Claim request created
- Status = 'PENDING'
- Finder can approve/reject

## 🔍 Verify Database

### Check Supabase Dashboard
1. Go to https://supabase.com/dashboard
2. Select your project
3. Go to Table Editor
4. Check tables:
   - `users` - Should have test users
   - `items` - Should have test items
   - `claim_requests` - Should have test claims

### Run SQL Queries
```sql
-- Count users
SELECT COUNT(*) as user_count FROM users;

-- Count items by type
SELECT type, COUNT(*) as count 
FROM items 
GROUP BY type;

-- Check recent items
SELECT id, title, type, status, created_at 
FROM items 
ORDER BY created_at DESC 
LIMIT 10;

-- Check claim requests
SELECT id, item_id, owner_id, finder_id, status 
FROM claim_requests;
```

## 🐛 Troubleshooting

### Build Errors

**Error: "SDK location not found"**
```bash
# Create local.properties file
echo sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk > local.properties
```

**Error: "Gradle sync failed"**
- Check internet connection
- Verify Gradle version in `gradle/wrapper/gradle-wrapper.properties`
- In Android Studio: File → Invalidate Caches → Restart

**Error: "Package not found"**
- Sync Gradle: File → Sync Project with Gradle Files
- Clean project: Build → Clean Project

### Runtime Errors

**Error: "Supabase connection failed"**
- Check `SupabaseClient.kt` has correct URL and key
- Verify Supabase project is active
- Check network connection

**Error: "Firebase not initialized"**
- Add `google-services.json` to `app/` folder
- Sync Gradle files
- Rebuild project

**Error: "Permission denied"**
- Grant permissions in app settings
- Check AndroidManifest.xml has permissions declared

## 📊 Test Results Template

```
Test Date: __________
Tester: __________
Device/Emulator: __________
Android Version: __________

✅ Database Connection: [ ] Pass [ ] Fail
✅ Authentication: [ ] Pass [ ] Fail
✅ Registration: [ ] Pass [ ] Fail
✅ Report Lost Item: [ ] Pass [ ] Fail
✅ Report Found Item: [ ] Pass [ ] Fail
✅ Real-time Updates: [ ] Pass [ ] Fail
✅ Matching System: [ ] Pass [ ] Fail
✅ Claim Request: [ ] Pass [ ] Fail
✅ My Reports: [ ] Pass [ ] Fail
✅ Profile: [ ] Pass [ ] Fail

Issues Found:
1. __________
2. __________

Notes:
__________
```

## 🎯 Quick Test Script

```bash
# 1. Build
.\gradlew.bat assembleDebug

# 2. Install
adb install app\build\outputs\apk\debug\app-debug.apk

# 3. Launch
adb shell am start -n com.mgm.lostfound/.ui.splash.SplashActivity

# 4. Check logs
adb logcat | findstr "MGM\|LostFound\|Supabase\|Firebase"
```

## ✅ What's Ready

- ✅ Supabase database configured
- ✅ All tables created
- ✅ Real-time enabled
- ✅ App code complete
- ✅ Build configuration ready
- ⏳ Firebase setup needed (for auth)
- ⏳ Test execution (manual testing)

## 🚀 Next Steps

1. **Complete Firebase Setup** (see `FIREBASE_SUPABASE_SETUP.md`)
2. **Build the app** in Android Studio
3. **Run manual tests** using checklist above
4. **Verify database** in Supabase dashboard
5. **Test real-time** with multiple devices

## 📝 Notes

- TestSprite requires API key (not configured)
- Manual testing is recommended for Android apps
- Use Android Studio for best testing experience
- Supabase dashboard for database verification

