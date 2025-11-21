# Complete Setup: Firebase Auth + Supabase Database

## Quick Overview

- **Firebase**: Authentication (Google Sign-In) + Push Notifications
- **Supabase**: Real-time Database + Storage

## Step 1: Firebase Setup (5 minutes)

### 1.1 Create Firebase Project
1. Go to https://console.firebase.google.com
2. Click "Add project"
3. Name: "MGM Lost & Found"
4. Enable Google Analytics (optional)
5. Create project

### 1.2 Add Android App
1. Click "Add app" → Android
2. Package: `com.mgm.lostfound`
3. Download `google-services.json`
4. Place in `app/` folder

### 1.3 Enable Services
- **Authentication** → Enable Google Sign-In
- **Cloud Messaging** → Already enabled

### 1.4 Get SHA-1
```bash
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```
Add SHA-1 to Firebase → Project Settings → Your App

### 1.5 Get Web Client ID
1. Firebase → Authentication → Sign-in method → Google
2. Copy Web client ID
3. Update `app/src/main/res/values/google-services.xml`:
```xml
<string name="default_web_client_id">YOUR_CLIENT_ID.apps.googleusercontent.com</string>
```

## Step 2: Supabase Setup (5 minutes)

### 2.1 Create Supabase Project
1. Go to https://supabase.com
2. Sign up / Log in
3. New Project → "MGM Lost & Found"
4. Choose region
5. Create (wait 2-3 minutes)

### 2.2 Get Credentials
1. Settings → API
2. Copy:
   - Project URL: `https://xxxxx.supabase.co`
   - anon key: `eyJhbGci...`

### 2.3 Update App
Edit `app/src/main/java/com/mgm/lostfound/data/supabase/SupabaseClient.kt`:
```kotlin
private const val SUPABASE_URL = "https://xxxxx.supabase.co"
private const val SUPABASE_KEY = "eyJhbGci..."
```

### 2.4 Create Tables
Go to SQL Editor and run (see `SUPABASE_SETUP.md` for full SQL):
```sql
-- Users table
CREATE TABLE users (
    id TEXT PRIMARY KEY,
    email TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    student_id TEXT NOT NULL,
    phone TEXT,
    department TEXT,
    year TEXT,
    role TEXT DEFAULT 'STUDENT',
    profile_image_url TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Items table
CREATE TABLE items (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    type TEXT NOT NULL CHECK (type IN ('LOST', 'FOUND')),
    category TEXT NOT NULL,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    location TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    photo_urls TEXT[] DEFAULT '{}',
    serial_number TEXT,
    reward TEXT,
    qr_code TEXT,
    status TEXT DEFAULT 'ACTIVE',
    matched_items TEXT[] DEFAULT '{}',
    claim_request_id TEXT,
    contact_shared BOOLEAN DEFAULT false,
    finder_info TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Claim requests table
CREATE TABLE claim_requests (
    id TEXT PRIMARY KEY,
    item_id TEXT NOT NULL,
    owner_id TEXT NOT NULL,
    finder_id TEXT NOT NULL,
    status TEXT DEFAULT 'PENDING',
    owner_contact_shared BOOLEAN DEFAULT false,
    finder_contact_shared BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
```

### 2.5 Enable Real-time
1. Database → Replication
2. Enable for: `items`, `claim_requests`, `users`

### 2.6 Set Up Storage
1. Storage → New bucket
2. Name: `items`
3. Make it **Public**
4. Create

## Step 3: Security Policies

### Supabase RLS Policies
Run in SQL Editor:

```sql
-- Enable RLS
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE items ENABLE ROW LEVEL SECURITY;
ALTER TABLE claim_requests ENABLE ROW LEVEL SECURITY;

-- Users policies
CREATE POLICY "Users can read all" ON users FOR SELECT USING (true);
CREATE POLICY "Users can update own" ON users FOR UPDATE USING (auth.uid()::text = id);

-- Items policies
CREATE POLICY "Anyone can read items" ON items FOR SELECT USING (true);
CREATE POLICY "Authenticated can insert" ON items FOR INSERT WITH CHECK (true);
CREATE POLICY "Users can update own" ON items FOR UPDATE USING (auth.uid()::text = user_id);

-- Claim requests policies
CREATE POLICY "Users can read own claims" ON claim_requests FOR SELECT 
    USING (auth.uid()::text = owner_id OR auth.uid()::text = finder_id);
CREATE POLICY "Users can create claims" ON claim_requests FOR INSERT WITH CHECK (true);
```

## Step 4: Build & Test

1. Sync Gradle files
2. Build project
3. Run on device/emulator
4. Test:
   - Google Sign-In
   - Register user
   - Report item
   - Check Supabase tables

## Architecture

```
┌─────────────────┐
│   Android App  │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌─────────┐ ┌──────────┐
│Firebase │ │ Supabase  │
│         │ │           │
│ • Auth  │ │ • Database│
│ • FCM   │ │ • Real-time│
│         │ │ • Storage │
└─────────┘ └──────────┘
```

## What Works Now

✅ Firebase Google Sign-In
✅ User registration in Supabase
✅ Real-time item updates
✅ Push notifications (Firebase)
✅ Image storage (Supabase)
✅ Real-time matching
✅ Claim requests

## Troubleshooting

**"Invalid API key"**
- Check Supabase anon key is correct
- Verify in Settings → API

**"Table does not exist"**
- Run all SQL commands
- Check Table Editor

**Real-time not working**
- Enable replication (Step 2.5)
- Check RLS policies allow reads

**Auth not working**
- Verify SHA-1 added to Firebase
- Check Web Client ID in google-services.xml
- Verify google-services.json is correct

## Production Checklist

- [ ] Review RLS policies
- [ ] Set up backups
- [ ] Configure CORS (if needed)
- [ ] Set up monitoring
- [ ] Test on multiple devices
- [ ] Review security rules

## Support

- Firebase: https://firebase.google.com/docs
- Supabase: https://supabase.com/docs
- App Setup: See `SETUP_GUIDE.md`

🎉 You're ready to go!

