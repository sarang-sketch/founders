package com.mgm.lostfound.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object SupabaseClient {
    // Supabase project credentials (configured via MCP)
    private const val SUPABASE_URL = "https://wiwnzfiggrijlnkmrnjx.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Indpd256ZmlnZ3Jpamxua21ybmp4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjM2NDgzNDYsImV4cCI6MjA3OTIyNDM0Nn0.bW0LwPWxPjSpRqwx6LMQsxkskvELcMvSM3aOd8yfa6c"

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
            install(Storage)
        }
    }
}
