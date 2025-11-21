# Complete Setup Guide - MGM Lost & Found Android App

## Prerequisites
- Android Studio (latest version)
- JDK 8 or higher
- Firebase account
- Google Cloud Console account

## Step 1: Firebase Setup

### 1.1 Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Click "Add project"
3. Enter project name: "MGM Lost & Found"
4. Enable Google Analytics (optional)
5. Create project

### 1.2 Add Android App
1. In Firebase Console, click "Add app" > Android
2. Package name: `com.mgm.lostfound`
3. App nickname: "MGM Lost & Found"
4. Download `google-services.json`
5. Place it in `app/` directory (replace the placeholder)

### 1.3 Enable Firebase Services

#### Authentication
1. Go to Authentication > Sign-in method
2. Enable "Google" sign-in
3. Add support email
4. Save

#### Firestore Database
1. Go to Firestore Database
2. Click "Create database"
3. Start in test mode (for development)
4. Choose location (closest to your region)
5. Enable

#### Firebase Storage
1. Go to Storage
2. Click "Get started"
3. Start in test mode
4. Choose location
5. Enable

#### Cloud Messaging (FCM)
1. Go to Cloud Messaging
2. Enable Cloud Messaging API
3. Note the Server Key (for backend)

### 1.4 Get SHA-1 Certificate
```bash
# For debug keystore (Windows)
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android

# For release keystore (create one first)
keytool -list -v -keystore your-release-key.keystore
```

1. Copy SHA-1 fingerprint
2. Go to Firebase Console > Project Settings > Your Android App
3. Add SHA-1 certificate fingerprint
4. Download updated `google-services.json`

## Step 2: Google Sign-In Setup

### 2.1 Configure OAuth Consent Screen
1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Select your Firebase project
3. Go to APIs & Services > OAuth consent screen
4. Choose "External" (for testing)
5. Fill in app information
6. Add scopes: `email`, `profile`
7. Add test users (your email)

### 2.2 Create OAuth 2.0 Credentials
1. Go to APIs & Services > Credentials
2. Click "Create Credentials" > OAuth client ID
3. Application type: Web application
4. Name: "MGM Lost & Found Web Client"
5. Copy the Client ID
6. Update in `app/src/main/res/values/google-services.xml`:
```xml
<string name="default_web_client_id">YOUR_CLIENT_ID.apps.googleusercontent.com</string>
```

## Step 3: Supabase Setup (Optional)

### 3.1 Create Supabase Project
1. Go to [Supabase](https://supabase.com)
2. Create new project
3. Note your project URL and anon key

### 3.2 Update Supabase Configuration
1. Open `app/src/main/java/com/mgm/lostfound/data/supabase/SupabaseClient.kt`
2. Replace:
   - `YOUR_SUPABASE_URL` with your project URL
   - `YOUR_SUPABASE_ANON_KEY` with your anon key

## Step 4: Build Configuration

### 4.1 Update Build Files
1. Open `app/build.gradle`
2. Ensure all dependencies are synced
3. Sync project with Gradle files

### 4.2 Verify google-services.json
1. Ensure `google-services.json` is in `app/` directory
2. Verify package name matches: `com.mgm.lostfound`

## Step 5: Firestore Database Structure

Create these collections in Firestore:

### Collection: `users`
```
users/{userId}
  - id: string
  - email: string
  - name: string
  - studentId: string
  - phone: string
  - department: string
  - year: string
  - role: string (STUDENT, ADMIN, SECURITY)
  - profileImageUrl: string
  - createdAt: timestamp
```

### Collection: `items`
```
items/{itemId}
  - id: string
  - userId: string
  - type: string (LOST, FOUND)
  - category: string
  - title: string
  - description: string
  - location: {
      latitude: number
      longitude: number
      address: string
    }
  - photoUrls: array<string>
  - serialNumber: string
  - reward: string
  - qrCode: string
  - status: string (ACTIVE, CLAIMED, CLOSED, FLAGGED)
  - matchedItems: array<string>
  - claimRequestId: string
  - contactShared: boolean
  - finderInfo: string
  - createdAt: timestamp
  - updatedAt: timestamp
```

### Collection: `claim_requests`
```
claim_requests/{requestId}
  - id: string
  - itemId: string
  - ownerId: string
  - finderId: string
  - status: string (PENDING, APPROVED, REJECTED, COMPLETED)
  - ownerContactShared: boolean
  - finderContactShared: boolean
  - createdAt: timestamp
```

## Step 6: Firestore Security Rules

Update Firestore Rules:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users collection
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Items collection
    match /items/{itemId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update: if request.auth != null && 
        (request.auth.uid == resource.data.userId || 
         get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role in ['ADMIN', 'SECURITY']);
      allow delete: if request.auth != null && 
        (request.auth.uid == resource.data.userId || 
         get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role in ['ADMIN', 'SECURITY']);
    }
    
    // Claim requests
    match /claim_requests/{requestId} {
      allow read: if request.auth != null && 
        (request.auth.uid == resource.data.ownerId || 
         request.auth.uid == resource.data.finderId);
      allow create: if request.auth != null;
      allow update: if request.auth != null && 
        (request.auth.uid == resource.data.finderId || 
         get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role in ['ADMIN', 'SECURITY']);
    }
  }
}
```

## Step 7: Storage Security Rules

Update Storage Rules:
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /items/{itemId}/{fileName} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
  }
}
```

## Step 8: Build and Run

1. Open project in Android Studio
2. Sync Gradle files
3. Connect Android device or start emulator
4. Click "Run" or press Shift+F10

## Step 9: Testing

### Test Authentication
1. Run app
2. Click "Sign in with Google"
3. Select Google account
4. Complete registration form
5. Verify user created in Firestore

### Test Item Reporting
1. Click "Report Lost" or "Report Found"
2. Fill in details
3. Take photo
4. Get location
5. Submit
6. Verify item created in Firestore

### Test Notifications
1. Report a lost item
2. Report a matching found item
3. Verify notification received

## Troubleshooting

### Build Errors
- Ensure `google-services.json` is in correct location
- Check package name matches everywhere
- Sync Gradle files

### Authentication Errors
- Verify SHA-1 certificate added to Firebase
- Check OAuth consent screen configured
- Verify web client ID in `google-services.xml`

### Database Errors
- Check Firestore rules
- Verify collections exist
- Check internet connection

### Notification Errors
- Verify FCM token generated
- Check notification permissions granted
- Verify notification channel created

## Production Deployment

1. Create release keystore
2. Update `build.gradle` with signing config
3. Generate signed APK
4. Test thoroughly
5. Upload to Google Play Store

## Support

For issues or questions, check:
- Firebase Documentation
- Android Developer Documentation
- Project README.md

