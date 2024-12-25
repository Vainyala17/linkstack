package com.hp77.linkstash.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.hp77.linkstash.data.local.entity.LinkEntity
import com.hp77.linkstash.data.local.entity.LinkTagCrossRef
import com.hp77.linkstash.data.local.relation.LinkWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface LinkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(link: LinkEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinks(links: List<LinkEntity>)

    @Update
    suspend fun updateLink(link: LinkEntity)

    @Delete
    suspend fun deleteLink(link: LinkEntity)

    @Query("SELECT * FROM links WHERE id = :linkId")
    suspend fun getLinkById(linkId: String): LinkEntity?

    @Transaction
    @Query("SELECT * FROM links WHERE id = :linkId")
    suspend fun getLinkWithTags(linkId: String): LinkWithTags?

    @Transaction
    @Query("SELECT * FROM links ORDER BY createdAt DESC")
    fun getAllLinksWithTags(): Flow<List<LinkWithTags>>

    @Transaction
    @Query("SELECT * FROM links WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getActiveLinks(): Flow<List<LinkWithTags>>

    @Transaction
    @Query("SELECT * FROM links WHERE isArchived = 1 ORDER BY createdAt DESC")
    fun getArchivedLinks(): Flow<List<LinkWithTags>>

    @Transaction
    @Query("SELECT * FROM links WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteLinks(): Flow<List<LinkWithTags>>

    @Transaction
    @Query("""
        SELECT DISTINCT l.* FROM links l
        LEFT JOIN link_tag_cross_ref lt ON l.id = lt.linkId
        WHERE (
            l.title LIKE '%' || :query || '%' 
            OR l.description LIKE '%' || :query || '%'
            OR l.url LIKE '%' || :query || '%'
        )
        AND (
            :tagIds = '' 
            OR lt.tagId IN (:tagIds)
        )
        ORDER BY l.createdAt DESC
    """)
    fun searchLinks(query: String, tagIds: List<String>): Flow<List<LinkWithTags>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinkTagCrossRef(crossRef: LinkTagCrossRef)

    @Delete
    suspend fun deleteLinkTagCrossRef(crossRef: LinkTagCrossRef)

    @Query("DELETE FROM link_tag_cross_ref WHERE linkId = :linkId")
    suspend fun deleteAllTagsForLink(linkId: String)
}
