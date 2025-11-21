# Android APK Testing Guide

## 🎯 Testing the APK

Since TestSprite is designed for web applications and requires a running server, here are the best ways to test your Android APK:

## ✅ Method 1: Manual Testing (Recommended)

### Step 1: Install APK
```bash
# Connect device via USB
adb install app\build\outputs\apk\debug\app-debug.apk

# Or transfer APK to device and install manually
```

### Step 2: Test Features
Follow the checklist in `TESTING_GUIDE.md`:
- ✅ Authentication
- ✅ Report Lost/Found items
- ✅ View items
- ✅ Real-time updates
- ✅ Matching system
- ✅ Claim requests

### Step 3: Verify in Supabase
- Check Supabase Dashboard → Table Editor
- Verify data is being created/updated
- Test real-time subscriptions

## ✅ Method 2: Android Studio Testing Tools

### Unit Tests
```kotlin
// Create in app/src/test/java/
class ItemRepositoryTest {
    @Test
    fun testReportItem() {
        // Test item creation
    }
}
```

### Instrumented Tests (UI Tests)
```kotlin
// Create in app/src/androidTest/java/
@RunWith(AndroidJUnit4::class)
class LoginActivityTest {
    @Test
    fun testDemoLogin() {
        // Test login flow
    }
}
```

### Run Tests
- Right-click test file → Run
- Or: Run → Run 'All Tests'

## ✅ Method 3: Test Supabase API (Backend Testing)

Since the app uses Supabase, you can test the API layer:

### Using Postman/Insomnia
1. Import `testsprite_tests/api_test_config.json`
2. Test all endpoints
3. Verify CRUD operations

### Using cURL
```bash
# Get users
curl -X GET "https://wiwnzfiggrijlnkmrnjx.supabase.co/rest/v1/users" \
  -H "apikey: YOUR_KEY" \
  -H "Authorization: Bearer YOUR_KEY"

# Create item
curl -X POST "https://wiwnzfiggrijlnkmrnjx.supabase.co/rest/v1/items" \
  -H "apikey: YOUR_KEY" \
  -H "Authorization: Bearer YOUR_KEY" \
  -H "Content-Type: application/json" \
  -d '{"id":"test1","user_id":"test_user_1","type":"LOST","category":"PHONE","title":"Test","description":"Test item"}'
```

## ✅ Method 4: TestSprite for API Testing

If you configure TestSprite API key:

1. **Test Supabase REST API endpoints**
   - Users API
   - Items API
   - Claim Requests API

2. **Test backend logic**
   - CRUD operations
   - Data validation
   - Real-time subscriptions

**Note**: TestSprite cannot test the Android UI directly, but can test the backend API that the app uses.

## 📊 Test Coverage

| Component | Test Method | Status |
|-----------|-------------|--------|
| Android UI | Manual/Espresso | ✅ Ready |
| Supabase API | TestSprite/Postman | ✅ Ready |
| Database | Supabase Dashboard | ✅ Ready |
| Real-time | Manual (2 devices) | ✅ Ready |
| Matching | Manual/API | ✅ Ready |

## 🚀 Quick Test Flow

1. **Build APK** (Android Studio)
2. **Install on device** (`adb install`)
3. **Test features** (Manual)
4. **Verify database** (Supabase Dashboard)
5. **Test API** (Postman/TestSprite if configured)

## 📝 Test Results

Document your test results:
- Which features work
- Any bugs found
- Database operations verified
- Real-time updates confirmed

## 🎯 Recommended Testing Approach

**For Android APK:**
1. ✅ Manual testing (most practical)
2. ✅ Android Studio testing tools
3. ✅ Supabase dashboard verification

**For Backend API:**
1. ✅ TestSprite (if API key configured)
2. ✅ Postman/Insomnia
3. ✅ cURL scripts

## ⚠️ TestSprite Limitation

TestSprite is designed for web applications. For Android apps:
- ✅ Can test Supabase API endpoints
- ❌ Cannot test Android UI directly
- ❌ Cannot test APK without web server wrapper

**Solution**: Use manual testing + API testing with TestSprite

