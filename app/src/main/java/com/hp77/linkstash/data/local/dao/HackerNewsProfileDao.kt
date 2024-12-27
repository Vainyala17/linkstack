package com.hp77.linkstash.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hp77.linkstash.data.local.entity.HackerNewsProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HackerNewsProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: HackerNewsProfileEntity)

    @Query("SELECT * FROM hackernews_profiles WHERE username = :username")
    fun getProfile(username: String): Flow<HackerNewsProfileEntity?>

    @Query("SELECT * FROM hackernews_profiles WHERE username = :username AND (lastFetchedAt + :maxAge) > :currentTime")
    suspend fun getProfileIfFresh(username: String, maxAge: Long, currentTime: Long = System.currentTimeMillis()): HackerNewsProfileEntity?

    @Query("DELETE FROM hackernews_profiles WHERE username = :username")
    suspend fun deleteProfile(username: String)
}
