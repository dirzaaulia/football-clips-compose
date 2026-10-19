package com.dirzaaulia.footballclips.data.constants

import com.dirzaaulia.footballclips.util.getBuildConfigString

actual object DatabaseConstants {
    actual val DATABASE_URL: String
        get() = getBuildConfigString("DATABASE_URL", getBuildConfigString("SUPABASE_URL", "https://edge.fc.dirzaaulia.com"))
    actual val DATABASE_ANON_KEY: String
        get() = getBuildConfigString("DATABASE_ANON_KEY", getBuildConfigString("SUPABASE_ANON_KEY", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVpb21rdHZhdm5kb3JhenJleWJhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc1OTQ4ODcsImV4cCI6MjEwMzE3MDg4N30.GhP7TJXMeAEpG8F-9pB_SLNwaXLjIrOJ2LAiPC1kCk4"))
    actual val GOOGLE_WEB_CLIENT_ID: String
        get() = getBuildConfigString("GOOGLE_WEB_CLIENT_ID", "519310318914-pushb78f44ug1a28m2oggunpck5annl8.apps.googleusercontent.com")
}
