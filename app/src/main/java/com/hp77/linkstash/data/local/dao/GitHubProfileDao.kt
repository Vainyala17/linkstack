package com.hp77.linkstash.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hp77.linkstash.data.local.entity.GitHubProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GitHubProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: GitHubProfileEntity)

    @Query("SELECT * FROM github_profiles WHERE login = :login")
    fun getProfile(login: String): Flow<GitHubProfileEntity?>

    @Query("SELECT * FROM github_profiles WHERE login = :login AND (lastFetchedAt + :maxAge) > :currentTime")
    suspend fun getProfileIfFresh(login: String, maxAge: Long, currentTime: Long = System.currentTimeMillis()): GitHubProfileEntity?

    @Query("DELETE FROM github_profiles WHERE login = :login")
    suspend fun deleteProfile(login: String)
}
