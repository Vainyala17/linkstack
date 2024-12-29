package com.hp77.linkstash.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "link_tag_cross_ref",
    primaryKeys = ["linkId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = LinkEntity::class,
            parentColumns = ["id"],
            childColumns = ["linkId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("linkId"),
        Index("tagId")
    ]
)
data class LinkTagCrossRef(
    val linkId: String,
    val tagId: String
)
