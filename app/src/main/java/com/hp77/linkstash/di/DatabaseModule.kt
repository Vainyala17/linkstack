package com.hp77.linkstash.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.concurrent.Executors
import com.hp77.linkstash.data.local.LinkStashDatabase
import com.hp77.linkstash.data.local.dao.LinkDao
import com.hp77.linkstash.data.local.dao.TagDao
import com.hp77.linkstash.data.local.migrations.MIGRATION_1_2
import com.hp77.linkstash.data.local.migrations.MIGRATION_2_3
import com.hp77.linkstash.data.local.migrations.MIGRATION_3_4
import com.hp77.linkstash.data.local.migrations.MIGRATION_4_5
import com.hp77.linkstash.data.local.migrations.MIGRATION_5_6
import com.hp77.linkstash.data.local.migrations.MIGRATION_6_7
import com.hp77.linkstash.data.local.dao.GitHubProfileDao
import com.hp77.linkstash.data.local.dao.HackerNewsProfileDao
import com.hp77.linkstash.data.local.util.DatabaseMaintenanceUtil
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
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
            .fallbackToDestructiveMigration() // Allow fallback if migration fails
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING) // Enable WAL mode
            .setQueryExecutor(Executors.newFixedThreadPool(4)) // Optimize thread pool
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Set optimal page and cache sizes
                    db.query("PRAGMA page_size = 4096").close()
                    db.query("PRAGMA cache_size = 2000").close() // ~8MB cache
                    db.query("PRAGMA synchronous = NORMAL").close() // Balanced durability and performance
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    // Ensure settings are maintained after reopening
                    db.query("PRAGMA page_size = 4096").close()
                    db.query("PRAGMA cache_size = 2000").close()
                    db.query("PRAGMA synchronous = NORMAL").close()
                }
            })
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

    @Provides
    @Singleton
    fun provideGitHubProfileDao(database: LinkStashDatabase): GitHubProfileDao {
        return database.gitHubProfileDao()
    }

    @Provides
    @Singleton
    fun provideHackerNewsProfileDao(database: LinkStashDatabase): HackerNewsProfileDao {
        return database.hackerNewsProfileDao()
    }

    @Provides
    @Singleton
    fun provideDatabaseMaintenanceUtil(
        @ApplicationContext context: Context,
        database: LinkStashDatabase
    ): DatabaseMaintenanceUtil {
        return DatabaseMaintenanceUtil(context, database)
    }
}
