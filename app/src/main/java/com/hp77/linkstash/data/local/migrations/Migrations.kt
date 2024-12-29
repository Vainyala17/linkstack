package com.hp77.linkstash.data.local.migrations

import androidx.room.migration.Migration
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.UUID

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add scrollPosition column with default value 0
        db.execSQL("ALTER TABLE links ADD COLUMN scrollPosition REAL NOT NULL DEFAULT 0")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create indices for foreign key columns in link_tag_cross_ref
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_link_tag_cross_ref_linkId` ON `link_tag_cross_ref` (`linkId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_link_tag_cross_ref_tagId` ON `link_tag_cross_ref` (`tagId`)")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create GitHub profiles table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS github_profiles (
                login TEXT PRIMARY KEY NOT NULL,
                name TEXT,
                avatarUrl TEXT NOT NULL,
                bio TEXT,
                location TEXT,
                publicRepos INTEGER NOT NULL,
                followers INTEGER NOT NULL,
                following INTEGER NOT NULL,
                createdAt TEXT NOT NULL,
                lastFetchedAt INTEGER NOT NULL
            )
        """)

        // Create HackerNews profiles table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS hackernews_profiles (
                username TEXT PRIMARY KEY NOT NULL,
                karma INTEGER NOT NULL,
                about TEXT,
                createdAt INTEGER NOT NULL,
                lastFetchedAt INTEGER NOT NULL
            )
        """)
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add notes field
        db.execSQL("ALTER TABLE links ADD COLUMN notes TEXT")
        
        // Add HackerNews integration columns
        db.execSQL("ALTER TABLE links ADD COLUMN hackerNewsId TEXT")
        db.execSQL("ALTER TABLE links ADD COLUMN hackerNewsUrl TEXT")
        
        // Add GitHub sync columns
        db.execSQL("ALTER TABLE links ADD COLUMN lastSyncedAt INTEGER")
        db.execSQL("ALTER TABLE links ADD COLUMN syncError TEXT")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add type column with default value OTHER
        db.execSQL("ALTER TABLE links ADD COLUMN type TEXT NOT NULL DEFAULT 'OTHER'")
        
        // Add isCompleted column with default value 0 (false)
        db.execSQL("ALTER TABLE links ADD COLUMN isCompleted INTEGER NOT NULL DEFAULT 0")
        
        // Add completedAt column (nullable)
        db.execSQL("ALTER TABLE links ADD COLUMN completedAt INTEGER")
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create the tags table if it doesn't exist
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS tags (
                id TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                color TEXT,
                createdAt INTEGER NOT NULL
            )
        """)

        // Create the link_tag_cross_ref table if it doesn't exist
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS link_tag_cross_ref (
                linkId TEXT NOT NULL,
                tagId TEXT NOT NULL,
                PRIMARY KEY(linkId, tagId),
                FOREIGN KEY(linkId) REFERENCES links(id) ON DELETE CASCADE,
                FOREIGN KEY(tagId) REFERENCES tags(id) ON DELETE CASCADE
            )
        """)

        // Create a temporary table with the new schema
        db.execSQL("""
            CREATE TABLE links_new (
                id TEXT PRIMARY KEY NOT NULL,
                url TEXT NOT NULL,
                title TEXT,
                description TEXT,
                previewImageUrl TEXT,
                createdAt INTEGER NOT NULL,
                reminderTime INTEGER,
                isArchived INTEGER NOT NULL,
                isFavorite INTEGER NOT NULL
            )
        """)

        // Copy data from old table to new table
        db.execSQL("""
            INSERT INTO links_new (
                id, url, title, description, previewImageUrl,
                createdAt, reminderTime, isArchived, isFavorite
            )
            SELECT 
                id, url, title, description, previewImageUrl,
                createdAt, reminderTime, isArchived, isFavorite
            FROM links
        """)

        // Try to migrate any existing tags from the old links table
        try {
            // Get tags from old links table (assuming they were stored in a 'tags' column)
            val cursor = db.query("SELECT id, tags FROM links")
            if (cursor.moveToFirst()) {
                do {
                    val linkId = cursor.getString(cursor.getColumnIndexOrThrow("id"))
                    val tagsString = cursor.getString(cursor.getColumnIndexOrThrow("tags"))
                    
                    // Parse tags and create entries
                    tagsString?.split(",")?.map { it.trim() }?.forEach { tagName ->
                        if (tagName.isNotEmpty()) {
                            val tagId = UUID.randomUUID().toString()
                            
                            // Insert tag
                            db.execSQL("""
                                INSERT OR IGNORE INTO tags (id, name, createdAt)
                                VALUES (?, ?, ?)
                            """, arrayOf(tagId, tagName, System.currentTimeMillis()))
                            
                            // Create cross reference
                            db.execSQL("""
                                INSERT OR IGNORE INTO link_tag_cross_ref (linkId, tagId)
                                VALUES (?, ?)
                            """, arrayOf(linkId, tagId))
                        }
                    }
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: Exception) {
            // If anything goes wrong during tag migration, just log it and continue
            android.util.Log.e("Migration", "Failed to migrate tags: ${e.message}")
        }

        // Drop old table
        db.execSQL("DROP TABLE links")

        // Rename new table to original name
        db.execSQL("ALTER TABLE links_new RENAME TO links")
    }
}
