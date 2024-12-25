package com.hp77.linkstash.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.hp77.linkstash.data.local.entity.LinkEntity
import com.hp77.linkstash.data.local.entity.LinkTagCrossRef
import com.hp77.linkstash.data.local.entity.TagEntity

data class LinkWithTags(
    @Embedded val link: LinkEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = LinkTagCrossRef::class,
            parentColumn = "linkId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>
)

data class TagWithLinks(
    @Embedded val tag: TagEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = LinkTagCrossRef::class,
            parentColumn = "tagId",
            entityColumn = "linkId"
        )
    )
    val links: List<LinkEntity>
)
