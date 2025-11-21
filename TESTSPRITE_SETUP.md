# TestSprite Setup for Android App

## ⚠️ Important Note

TestSprite is designed for **web applications** (frontend/backend servers). This is an **Android app** that doesn't run as a web server.

## 🔄 Solution: Test the Backend API Layer

Since the app uses **Supabase** as the backend, we can test the Supabase REST API endpoints directly.

### Supabase API Endpoints

The app uses these Supabase endpoints:
- `https://wiwnzfiggrijlnkmrnjx.supabase.co/rest/v1/users`
- `https://wiwnzfiggrijlnkmrnjx.supabase.co/rest/v1/items`
- `https://wiwnzfiggrijlnkmrnjx.supabase.co/rest/v1/claim_requests`

## 🧪 Testing Options

### Option 1: Test Supabase API Directly
TestSprite can test the REST API endpoints that the Android app uses.

### Option 2: Manual APK Testing
Since TestSprite requires a web server, for Android app testing:
- Use Android Studio's built-in testing
- Use Espresso for UI tests
- Manual testing with the APK

### Option 3: Create Test Server (Advanced)
Create a simple Node.js/Express server that exposes the app's logic for testing.

## 📋 Current Status

**TestSprite Limitation:**
- ❌ Cannot test Android APK directly (needs web server)
- ✅ Can test Supabase API endpoints
- ✅ Can test backend logic if exposed as API

## 🎯 Recommended Approach

1. **Test Supabase API** using TestSprite (if API key configured)
2. **Test Android APK** manually or with Android testing tools
3. **Integration testing** by testing the full flow

## 🔑 TestSprite API Key Required

To use TestSprite, you need:
1. Visit: https://www.testsprite.com/dashboard/settings/apikey
2. Create API key
3. Configure in TestSprite

## ✅ Alternative: Manual Testing

Since TestSprite requires setup, use:
- `TESTING_GUIDE.md` - Manual testing checklist
- Android Studio testing tools
- Supabase dashboard for database verification

