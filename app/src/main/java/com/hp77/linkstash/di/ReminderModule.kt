package com.hp77.linkstash.di

import android.content.Context
import android.util.Log
import androidx.work.WorkManager
import com.hp77.linkstash.util.ReminderManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReminderModule {

    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager {
        Log.d("ReminderModule", "Providing WorkManager instance")
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideReminderManager(
        @ApplicationContext context: Context,
        workManager: WorkManager
    ): ReminderManager {
        Log.d("ReminderModule", "Creating ReminderManager with injected WorkManager")
        return ReminderManager(context, workManager)
    }
}
