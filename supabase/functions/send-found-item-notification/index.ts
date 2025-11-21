// Supabase Edge Function to send FCM notifications when a found item is registered
// This function should be triggered by a database webhook or trigger

import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'

const FCM_SERVER_KEY = Deno.env.get('FCM_SERVER_KEY') || ''

interface FoundItem {
  id: string
  title: string
  type: string
  user_id: string
}

serve(async (req) => {
  try {
    // Get the found item from the request
    const { record }: { record: FoundItem } = await req.json()
    
    if (!record || record.type !== 'FOUND') {
      return new Response(
        JSON.stringify({ error: 'Not a found item' }),
        { status: 400, headers: { 'Content-Type': 'application/json' } }
      )
    }

    // Initialize Supabase client
    const supabaseUrl = Deno.env.get('SUPABASE_URL') || ''
    const supabaseKey = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') || ''
    const supabase = createClient(supabaseUrl, supabaseKey)

    // Get all user FCM tokens (excluding the user who created the item)
    const { data: users, error } = await supabase
      .from('users')
      .select('fcm_token')
      .not('fcm_token', 'is', null)
      .neq('id', record.user_id)

    if (error) {
      throw error
    }

    const tokens = users?.map(u => u.fcm_token).filter(Boolean) || []

    if (tokens.length === 0) {
      return new Response(
        JSON.stringify({ message: 'No FCM tokens found' }),
        { status: 200, headers: { 'Content-Type': 'application/json' } }
      )
    }

    // Send FCM notification to all tokens
    const fcmUrl = 'https://fcm.googleapis.com/fcm/send'
    const notification = {
      title: `Found Item: ${record.title}`,
      body: `Found ${record.title} - is it yours?`,
      sound: 'default',
      priority: 'high'
    }

    const payload = {
      registration_ids: tokens,
      notification: notification,
      data: {
        type: 'found',
        itemId: record.id,
        title: record.title
      }
    }

    const fcmResponse = await fetch(fcmUrl, {
      method: 'POST',
      headers: {
        'Authorization': `key=${FCM_SERVER_KEY}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    })

    if (!fcmResponse.ok) {
      throw new Error(`FCM request failed: ${fcmResponse.statusText}`)
    }

    const fcmResult = await fcmResponse.json()

    return new Response(
      JSON.stringify({ 
        success: true, 
        tokensSent: tokens.length,
        fcmResult 
      }),
      { status: 200, headers: { 'Content-Type': 'application/json' } }
    )
  } catch (error) {
    return new Response(
      JSON.stringify({ error: error.message }),
      { status: 500, headers: { 'Content-Type': 'application/json' } }
    )
  }
})

