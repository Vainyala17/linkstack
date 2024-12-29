package com.hp77.linkstash.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hp77.linkstash.data.local.converter.Converters
import com.hp77.linkstash.data.local.dao.*
import com.hp77.linkstash.data.local.entity.*

@Database(
    entities = [
        LinkEntity::class,
        TagEntity::class,
        LinkTagCrossRef::class,
        GitHubProfileEntity::class,
        HackerNewsProfileEntity::class
    ],
    version = 7,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class LinkStashDatabase : RoomDatabase() {
    abstract fun linkDao(): LinkDao
    abstract fun tagDao(): TagDao
    abstract fun gitHubProfileDao(): GitHubProfileDao
    abstract fun hackerNewsProfileDao(): HackerNewsProfileDao

    companion object {
        const val DATABASE_NAME = "linkstash.db"
    }

    @androidx.room.Transaction
    suspend fun importLinkWithTags(link: LinkEntity, tags: List<TagEntity>, tagRefs: List<LinkTagCrossRef>) {
        // First insert all tags
        tags.forEach { tag ->
            tagDao().insertTag(tag)
        }
        // Then insert link
        linkDao().insertLink(link)
        // Finally create tag relationships
        tagRefs.forEach { ref ->
            linkDao().insertLinkTagCrossRef(ref)
        }
    }
}
