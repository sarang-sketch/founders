# Testing Guide - MGM Lost & Found Android App

## 🧪 Test Setup

### Prerequisites
- Android Studio installed
- Android device or emulator
- Firebase project configured
- Supabase database set up (already done via MCP)

## 📱 Manual Testing Checklist

### 1. Authentication Tests

#### Test 1.1: Demo Login
- [ ] Launch app
- [ ] Tap "Demo Login" button
- [ ] Verify user is logged in
- [ ] Check user data in Supabase `users` table

#### Test 1.2: Google Sign-In
- [ ] Tap "Sign in with Google"
- [ ] Select Google account
- [ ] If new user, verify registration screen appears
- [ ] Complete registration form
- [ ] Verify user created in Supabase

#### Test 1.3: Registration Validation
- [ ] Try to register with empty fields
- [ ] Verify validation errors appear
- [ ] Complete all required fields
- [ ] Verify successful registration

### 2. Lost Items Tests

#### Test 2.1: Report Lost Item
- [ ] Navigate to Lost Items tab
- [ ] Tap FAB to report lost item
- [ ] Fill in title, description, category
- [ ] Take photo (if camera available)
- [ ] Get current location
- [ ] Add optional serial number/reward
- [ ] Submit
- [ ] Verify item appears in list
- [ ] Check Supabase `items` table

#### Test 2.2: View Lost Items
- [ ] Navigate to Lost Items tab
- [ ] Verify items load from Supabase
- [ ] Pull to refresh
- [ ] Verify real-time updates (open in another device/emulator)

#### Test 2.3: Item Details
- [ ] Tap on a lost item
- [ ] Verify all details displayed
- [ ] Check photo loads correctly
- [ ] Verify location shown

### 3. Found Items Tests

#### Test 3.1: Report Found Item
- [ ] Navigate to Found Items tab
- [ ] Tap FAB to report found item
- [ ] Fill in all details
- [ ] Submit
- [ ] Verify item appears in Found Items list

#### Test 3.2: View Found Items
- [ ] Navigate to Found Items tab
- [ ] Verify items load
- [ ] Test real-time updates

### 4. Matching System Tests

#### Test 4.1: Automatic Matching
- [ ] Report a lost item (e.g., "iPhone 13, black case")
- [ ] Report a found item with similar description
- [ ] Verify matching algorithm triggers
- [ ] Check `matched_items` array in Supabase
- [ ] Verify notification received

#### Test 4.2: Category Matching
- [ ] Report lost item with category "PHONE"
- [ ] Report found item with category "PHONE"
- [ ] Verify match occurs

#### Test 4.3: Location Matching
- [ ] Report lost item with location
- [ ] Report found item within 500m
- [ ] Verify location-based match

### 5. Claim System Tests

#### Test 5.1: Create Claim Request
- [ ] View a found item
- [ ] Tap "Claim" button
- [ ] Submit claim request
- [ ] Verify request created in Supabase `claim_requests` table

#### Test 5.2: Approve Claim
- [ ] As finder, view claim requests
- [ ] Approve a claim request
- [ ] Verify status updated to "APPROVED"
- [ ] Verify contact sharing enabled

### 6. My Reports Tests

#### Test 6.1: View My Reports
- [ ] Navigate to "My Reports" tab
- [ ] Verify only user's items shown
- [ ] Verify both lost and found items appear

### 7. Profile Tests

#### Test 7.1: View Profile
- [ ] Navigate to Profile tab
- [ ] Verify all user information displayed
- [ ] Check student ID, department, year

### 8. Admin Tests

#### Test 8.1: Admin Dashboard
- [ ] Login as admin user
- [ ] Access admin dashboard
- [ ] View all reports
- [ ] Test close case functionality
- [ ] Test flag case functionality

### 9. Real-time Tests

#### Test 9.1: Real-time Updates
- [ ] Open app on two devices/emulators
- [ ] Report item on device 1
- [ ] Verify item appears on device 2 without refresh
- [ ] Test with multiple items

### 10. Notifications Tests

#### Test 10.1: Push Notifications
- [ ] Grant notification permissions
- [ ] Report matching item
- [ ] Verify notification received
- [ ] Check notification sound (different for lost/found)

## 🔧 Automated Testing

### Unit Tests (To be created)

```kotlin
// Example test structure
class ItemRepositoryTest {
    @Test
    fun testReportItem() {
        // Test item creation
    }
    
    @Test
    fun testGetItems() {
        // Test item retrieval
    }
}
```

### Integration Tests

Test Supabase connection:
```kotlin
@Test
fun testSupabaseConnection() {
    // Verify Supabase client connects
    // Verify tables accessible
}
```

## 📊 Database Testing

### Verify Supabase Tables

Run these SQL queries in Supabase SQL Editor:

```sql
-- Check users table
SELECT COUNT(*) FROM users;

-- Check items table
SELECT COUNT(*), type, status FROM items GROUP BY type, status;

-- Check claim_requests table
SELECT COUNT(*), status FROM claim_requests GROUP BY status;

-- Test real-time
-- Open app and insert a record, verify it appears
```

## 🐛 Common Issues & Solutions

### Issue: Items not loading
- Check Supabase connection
- Verify RLS policies allow reads
- Check network connection

### Issue: Real-time not working
- Verify replication enabled in Supabase
- Check WebSocket connection

### Issue: Notifications not received
- Verify FCM token generated
- Check notification permissions
- Verify Firebase Cloud Messaging setup

## ✅ Test Results Template

```
Test Date: __________
Tester: __________
Device: __________
Android Version: __________

Authentication: [ ] Pass [ ] Fail
Lost Items: [ ] Pass [ ] Fail
Found Items: [ ] Pass [ ] Fail
Matching: [ ] Pass [ ] Fail
Claim System: [ ] Pass [ ] Fail
Real-time: [ ] Pass [ ] Fail
Notifications: [ ] Pass [ ] Fail

Issues Found:
1. __________
2. __________
```

## 🚀 Quick Test Run

1. Build app: `./gradlew assembleDebug`
2. Install on device: `adb install app/build/outputs/apk/debug/app-debug.apk`
3. Launch app
4. Test demo login
5. Report a test item
6. Verify in Supabase dashboard

## 📝 Notes

- Test on multiple Android versions (API 24+)
- Test on different screen sizes
- Test with/without network
- Test permission flows
- Test error handling

