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

    @Query("SELECT * FROM links WHERE url = :url LIMIT 1")
    suspend fun getLinkByUrl(url: String): LinkEntity?

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
        WHERE (
            l.title LIKE '%' || :query || '%' 
            OR l.description LIKE '%' || :query || '%'
            OR l.url LIKE '%' || :query || '%'
        )
        ORDER BY l.createdAt DESC
    """)
    fun searchLinks(query: String): Flow<List<LinkWithTags>>

    @Transaction
    @Query("""
        SELECT DISTINCT l.* FROM links l
        LEFT JOIN link_tag_cross_ref lt ON l.id = lt.linkId
        WHERE (
            l.title LIKE '%' || :query || '%' 
            OR l.description LIKE '%' || :query || '%'
            OR l.url LIKE '%' || :query || '%'
        )
        AND lt.tagId IN (:tagIds)
        GROUP BY l.id
        HAVING COUNT(DISTINCT lt.tagId) > 0
        ORDER BY l.createdAt DESC
    """)
    fun searchLinksWithTags(query: String, tagIds: List<String>): Flow<List<LinkWithTags>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinkTagCrossRef(crossRef: LinkTagCrossRef)

    @Delete
    suspend fun deleteLinkTagCrossRef(crossRef: LinkTagCrossRef)

    @Query("DELETE FROM link_tag_cross_ref WHERE linkId = :linkId")
    suspend fun deleteAllTagsForLink(linkId: String)

    @Query("UPDATE links SET scrollPosition = :position WHERE id = :linkId")
    suspend fun updateScrollPosition(linkId: String, position: Float)

    // Sync-related methods
    @Query("UPDATE links SET lastSyncedAt = :timestamp, syncError = NULL WHERE id = :linkId")
    suspend fun updateSyncSuccess(linkId: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE links SET syncError = :error WHERE id = :linkId")
    suspend fun updateSyncError(linkId: String, error: String)

    @Query("UPDATE links SET lastSyncedAt = NULL, syncError = NULL WHERE id = :linkId")
    suspend fun clearSyncStatus(linkId: String)

    @Query("SELECT * FROM links WHERE lastSyncedAt IS NULL OR syncError IS NOT NULL")
    suspend fun getUnsyncedLinks(): List<LinkEntity>

    // Batch sync status updates
    @Query("UPDATE links SET lastSyncedAt = :timestamp, syncError = NULL WHERE id IN (:linkIds)")
    suspend fun updateSyncSuccessBatch(linkIds: List<String>, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE links SET syncError = :error WHERE id IN (:linkIds)")
    suspend fun updateSyncErrorBatch(linkIds: List<String>, error: String)

    @Query("UPDATE links SET lastSyncedAt = NULL, syncError = NULL WHERE id IN (:linkIds)")
    suspend fun clearSyncStatusBatch(linkIds: List<String>)

    @Query("DELETE FROM links WHERE id IS NULL OR id = ''")
    suspend fun deleteInvalidLinks(): Int
}
