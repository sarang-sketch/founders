# Notification Setup Guide

This document explains how to set up notifications for the Lost & Found app.

## Features Implemented

### 1. Found Item Notifications
When a user registers a **FOUND** item, all logged-in users should receive a notification saying "Found [item name] - is it yours?"

### 2. Claim Request Notifications
When someone claims a lost device, the person who registered the lost item receives a notification with the username of the person claiming it.

## Current Implementation

### App-Side (Android)
- ✅ FCM token storage in Supabase `users` table
- ✅ FCM topic subscription (`found_items` topic)
- ✅ Local notifications when new found items arrive via real-time subscription
- ✅ Claim notifications with username included
- ✅ FCM token saved on login/registration

### Backend Required
To send push notifications to all users when a found item is registered, you need to set up one of the following:

## Option 1: Supabase Edge Function (Recommended)

### Setup Steps

1. **Install Supabase CLI** (if not already installed):
```bash
npm install -g supabase
```

2. **Initialize Supabase Functions** (if not already done):
```bash
supabase functions new send-found-item-notification
```

3. **Copy the Edge Function**:
   - Copy `supabase/functions/send-found-item-notification/index.ts` to your Supabase functions directory

4. **Set Environment Variables**:
```bash
supabase secrets set FCM_SERVER_KEY=your_fcm_server_key_here
```

5. **Deploy the Function**:
```bash
supabase functions deploy send-found-item-notification
```

6. **Create Database Trigger**:
   Create a PostgreSQL trigger in Supabase that calls the Edge Function when a new FOUND item is inserted:

```sql
-- Create a function to call the Edge Function
CREATE OR REPLACE FUNCTION notify_found_item()
RETURNS TRIGGER AS $$
BEGIN
  -- Call Supabase Edge Function via HTTP
  PERFORM
    net.http_post(
      url := 'https://YOUR_PROJECT_REF.supabase.co/functions/v1/send-found-item-notification',
      headers := jsonb_build_object(
        'Content-Type', 'application/json',
        'Authorization', 'Bearer ' || current_setting('app.settings.service_role_key')
      ),
      body := jsonb_build_object('record', row_to_json(NEW))
    );
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger
CREATE TRIGGER on_found_item_insert
  AFTER INSERT ON items
  FOR EACH ROW
  WHEN (NEW.type = 'FOUND')
  EXECUTE FUNCTION notify_found_item();
```

### Option 2: Firebase Cloud Functions

Create a Firebase Cloud Function that:
1. Listens to new items in Supabase (via webhook or polling)
2. Sends FCM notifications to the `found_items` topic when a FOUND item is registered

### Option 3: Custom Backend Service

Create a backend service (Node.js, Python, etc.) that:
1. Listens to Supabase webhooks for new items
2. Sends FCM notifications using the FCM Admin SDK or REST API

## Database Schema Update

The `users` table in Supabase needs an `fcm_token` column:

```sql
ALTER TABLE users ADD COLUMN IF NOT EXISTS fcm_token TEXT;
```

## Testing Notifications

### Test Found Item Notification:
1. Login as User A
2. Login as User B (on another device)
3. User A registers a FOUND item
4. User B should receive a notification: "Found [item name] - is it yours?"

### Test Claim Notification:
1. User A registers a LOST item
2. User B registers a FOUND item matching User A's lost item
3. User A claims the found item
4. User B should receive a notification: "[User A's name] wants to claim the item you found"

## Current Status

✅ **Claim Notifications**: Fully implemented with username
✅ **FCM Token Storage**: Implemented
✅ **Topic Subscription**: Implemented
⚠️ **Broadcast Notifications**: Requires backend setup (Edge Function/Cloud Function)

## Notes

- FCM Server Key should be kept secure and never exposed in client code
- The Edge Function approach is recommended as it keeps everything in Supabase
- Local notifications work immediately via real-time subscriptions
- Push notifications require the backend service to be set up

