package com.hp77.linkstash.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.hp77.linkstash.data.local.entity.TagEntity
import com.hp77.linkstash.data.local.relation.TagWithLinks
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<TagEntity>)

    @Update
    suspend fun updateTag(tag: TagEntity)

    @Delete
    suspend fun deleteTag(tag: TagEntity)

    @Query("SELECT * FROM tags WHERE id = :tagId")
    suspend fun getTagById(tagId: String): TagEntity?

    @Transaction
    @Query("SELECT * FROM tags WHERE id = :tagId")
    suspend fun getTagWithLinks(tagId: String): TagWithLinks?

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Transaction
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTagsWithLinks(): Flow<List<TagWithLinks>>

    @Query("SELECT * FROM tags WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchTags(query: String): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE id IN (SELECT tagId FROM link_tag_cross_ref WHERE linkId = :linkId)")
    fun getTagsForLink(linkId: String): Flow<List<TagEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM tags WHERE name = :name)")
    suspend fun isTagExists(name: String): Boolean

    @Query("SELECT COUNT(*) FROM link_tag_cross_ref WHERE tagId = :tagId")
    suspend fun getLinkedLinksCount(tagId: String): Int
}
