package com.hp77.linkstash.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hp77.linkstash.data.local.dao.LinkDao
import com.hp77.linkstash.data.local.dao.TagDao
import com.hp77.linkstash.data.local.entity.LinkEntity
import com.hp77.linkstash.data.local.entity.LinkTagCrossRef
import com.hp77.linkstash.data.local.entity.TagEntity

@Database(
    entities = [
        LinkEntity::class,
        TagEntity::class,
        LinkTagCrossRef::class
    ],
    version = 2,
    exportSchema = true
)
abstract class LinkStashDatabase : RoomDatabase() {
    abstract fun linkDao(): LinkDao
    abstract fun tagDao(): TagDao

    companion object {
        const val DATABASE_NAME = "linkstash.db"
    }
}
