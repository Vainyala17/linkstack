package com.hp77.linkstash.di

import android.content.Context
import com.hp77.linkstash.data.preferences.SearchPreferences
import com.hp77.linkstash.data.preferences.ThemePreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    @Provides
    @Singleton
    fun provideSearchPreferences(
        @ApplicationContext context: Context
    ): SearchPreferences {
        return SearchPreferences(context)
    }

    @Provides
    @Singleton
    fun provideThemePreferences(
        @ApplicationContext context: Context
    ): ThemePreferences {
        return ThemePreferences(context)
    }
}
