# Quick Start Guide

## Minimum Setup (5 minutes)

### 1. Firebase Setup (Required)
1. Go to https://console.firebase.google.com
2. Create new project: "MGM Lost & Found"
3. Add Android app with package: `com.mgm.lostfound`
4. Download `google-services.json`
5. Place in `app/` folder

### 2. Enable Services
In Firebase Console:
- **Authentication** → Enable Google Sign-In
- **Firestore** → Create database (test mode)
- **Storage** → Enable (test mode)
- **Cloud Messaging** → Already enabled

### 3. Get SHA-1 (For Google Sign-In)
```bash
# Windows
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```
Copy SHA-1 and add to Firebase → Project Settings → Your App

### 4. Get Web Client ID
1. Firebase Console → Authentication → Sign-in method → Google
2. Copy Web client ID
3. Update `app/src/main/res/values/google-services.xml`:
```xml
<string name="default_web_client_id">YOUR_CLIENT_ID.apps.googleusercontent.com</string>
```

### 5. Build & Run
```bash
# In Android Studio
1. File → Sync Project with Gradle Files
2. Run → Run 'app'
```

## That's It! 🎉

The app will work with:
- ✅ Google Authentication
- ✅ Firestore Database
- ✅ Firebase Storage (for images)
- ✅ Push Notifications
- ✅ Real-time matching

## Optional: Supabase Setup
If you want to use Supabase as well:
1. Create project at https://supabase.com
2. Update `SupabaseClient.kt` with your URL and key

## Testing
1. Run app
2. Click "Demo Login" (works without setup)
3. Or use "Sign in with Google" (requires setup above)

## Need Help?
See `SETUP_GUIDE.md` for detailed instructions.

