package com.hp77.linkstash.di

import android.content.Context
import androidx.room.Room
import com.hp77.linkstash.data.local.LinkStashDatabase
import com.hp77.linkstash.data.local.dao.LinkDao
import com.hp77.linkstash.data.local.dao.TagDao
import com.hp77.linkstash.data.local.migrations.MIGRATION_1_2
import com.hp77.linkstash.data.local.migrations.MIGRATION_2_3
import com.hp77.linkstash.data.local.migrations.MIGRATION_3_4
import com.hp77.linkstash.data.repository.LinkRepositoryImpl
import com.hp77.linkstash.data.repository.TagRepositoryImpl
import com.hp77.linkstash.domain.repository.LinkRepository
import com.hp77.linkstash.domain.repository.TagRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): LinkStashDatabase {
        return Room.databaseBuilder(
            context,
            LinkStashDatabase::class.java,
            LinkStashDatabase.DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .fallbackToDestructiveMigration() // Allow fallback if migration fails
            .build()
    }

    @Provides
    @Singleton
    fun provideLinkDao(database: LinkStashDatabase): LinkDao {
        return database.linkDao()
    }

    @Provides
    @Singleton
    fun provideTagDao(database: LinkStashDatabase): TagDao {
        return database.tagDao()
    }

    @Provides
    @Singleton
    fun provideLinkRepository(linkDao: LinkDao): LinkRepository {
        return LinkRepositoryImpl(linkDao)
    }

    @Provides
    @Singleton
    fun provideTagRepository(tagDao: TagDao, linkDao: LinkDao): TagRepository {
        return TagRepositoryImpl(tagDao, linkDao)
    }
}
