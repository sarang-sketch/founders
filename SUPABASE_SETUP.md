# Supabase Database Setup Guide

## Step 1: Create Supabase Project

1. Go to [Supabase](https://supabase.com)
2. Sign up or log in
3. Click "New Project"
4. Fill in:
   - **Name**: MGM Lost & Found
   - **Database Password**: (choose a strong password)
   - **Region**: (choose closest to you)
5. Click "Create new project"
6. Wait for project to be ready (2-3 minutes)

## Step 2: Get Project Credentials

1. In Supabase Dashboard, go to **Settings** → **API**
2. Copy:
   - **Project URL** (e.g., `https://xxxxx.supabase.co`)
   - **anon/public key** (starts with `eyJ...`)

## Step 3: Update App Configuration

1. Open `app/src/main/java/com/mgm/lostfound/data/supabase/SupabaseClient.kt`
2. Replace:
   ```kotlin
   private const val SUPABASE_URL = "YOUR_SUPABASE_URL"
   private const val SUPABASE_KEY = "YOUR_SUPABASE_ANON_KEY"
   ```
3. With your actual values:
   ```kotlin
   private const val SUPABASE_URL = "https://xxxxx.supabase.co"
   private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
   ```

## Step 4: Create Database Tables

Go to **SQL Editor** in Supabase and run these SQL commands:

### Users Table
```sql
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

-- Enable Row Level Security
ALTER TABLE users ENABLE ROW LEVEL SECURITY;

-- Policy: Users can read all, but only update their own
CREATE POLICY "Users can read all profiles"
    ON users FOR SELECT
    USING (true);

CREATE POLICY "Users can update own profile"
    ON users FOR UPDATE
    USING (auth.uid()::text = id);
```

### Items Table
```sql
CREATE TABLE items (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id),
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
    status TEXT DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'CLAIMED', 'CLOSED', 'FLAGGED')),
    matched_items TEXT[] DEFAULT '{}',
    claim_request_id TEXT,
    contact_shared BOOLEAN DEFAULT false,
    finder_info TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Enable Row Level Security
ALTER TABLE items ENABLE ROW LEVEL SECURITY;

-- Policy: Anyone authenticated can read items
CREATE POLICY "Anyone can read items"
    ON items FOR SELECT
    USING (true);

-- Policy: Authenticated users can insert items
CREATE POLICY "Authenticated users can insert items"
    ON items FOR INSERT
    WITH CHECK (auth.role() = 'authenticated');

-- Policy: Users can update their own items
CREATE POLICY "Users can update own items"
    ON items FOR UPDATE
    USING (auth.uid()::text = user_id);
```

### Claim Requests Table
```sql
CREATE TABLE claim_requests (
    id TEXT PRIMARY KEY,
    item_id TEXT NOT NULL REFERENCES items(id),
    owner_id TEXT NOT NULL REFERENCES users(id),
    finder_id TEXT NOT NULL REFERENCES users(id),
    status TEXT DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED')),
    owner_contact_shared BOOLEAN DEFAULT false,
    finder_contact_shared BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Enable Row Level Security
ALTER TABLE claim_requests ENABLE ROW LEVEL SECURITY;

-- Policy: Users can read their own claim requests
CREATE POLICY "Users can read own claims"
    ON claim_requests FOR SELECT
    USING (auth.uid()::text = owner_id OR auth.uid()::text = finder_id);

-- Policy: Authenticated users can create claims
CREATE POLICY "Users can create claims"
    ON claim_requests FOR INSERT
    WITH CHECK (auth.role() = 'authenticated');

-- Policy: Finder can update claim status
CREATE POLICY "Finder can update claims"
    ON claim_requests FOR UPDATE
    USING (auth.uid()::text = finder_id);
```

## Step 5: Enable Real-time (Replication)

1. Go to **Database** → **Replication**
2. Enable replication for:
   - `items` table
   - `claim_requests` table
   - `users` table (optional)

This enables real-time updates in the app!

## Step 6: Set Up Storage (for Images)

1. Go to **Storage** in Supabase
2. Click "Create a new bucket"
3. Name: `items`
4. Make it **Public**
5. Click "Create bucket"

### Storage Policies
Go to **Storage** → **Policies** → `items` bucket:

```sql
-- Allow public read access
CREATE POLICY "Public read access"
    ON storage.objects FOR SELECT
    USING (bucket_id = 'items');

-- Allow authenticated users to upload
CREATE POLICY "Authenticated users can upload"
    ON storage.objects FOR INSERT
    WITH CHECK (bucket_id = 'items' AND auth.role() = 'authenticated');

-- Allow users to update their own uploads
CREATE POLICY "Users can update own uploads"
    ON storage.objects FOR UPDATE
    USING (bucket_id = 'items' AND auth.uid()::text = (storage.foldername(name))[1]);
```

## Step 7: Enable Firebase Authentication in Supabase

Since we're using Firebase Auth, we need to link it:

1. Go to **Authentication** → **Providers**
2. Enable **Email** provider (for Firebase users)
3. Or set up **Custom JWT** if using Firebase tokens

## Step 8: Test Connection

1. Build and run the app
2. Try to register a user
3. Check Supabase **Table Editor** → `users` table
4. You should see the new user!

## Real-time Features

The app now has:
- ✅ Real-time item updates
- ✅ Instant notifications when items are added
- ✅ Live matching system
- ✅ Real-time claim requests

## Troubleshooting

### "Invalid API key" error
- Check that you copied the **anon key**, not the service_role key
- Verify the key in Supabase Settings → API

### "Table does not exist" error
- Make sure you ran all SQL commands
- Check Table Editor to verify tables exist

### Real-time not working
- Enable replication for tables (Step 5)
- Check that RLS policies allow reads

### Storage upload fails
- Verify bucket is public
- Check storage policies
- Verify file size limits

## Security Notes

- The anon key is safe to use in the app (it's public)
- Row Level Security (RLS) protects your data
- Never commit service_role key to code
- Review RLS policies before production

## Next Steps

1. Test all features
2. Review RLS policies for production
3. Set up backups
4. Monitor usage in Supabase Dashboard

Your app now uses:
- **Firebase** for Authentication & Push Notifications
- **Supabase** for Real-time Database

🎉 You're all set!

