package com.hp77.linkstash.domain.usecase.tag

import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.model.Tag
import com.hp77.linkstash.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

sealed class TagOperation {
    data class Add(val tag: Tag) : TagOperation()
    data class Update(val tag: Tag) : TagOperation()
    data class Delete(val tag: Tag) : TagOperation()
    data class AddToLink(val tag: Tag, val link: Link) : TagOperation()
    data class RemoveFromLink(val tag: Tag, val link: Link) : TagOperation()
    data class GetOrCreate(val name: String) : TagOperation()
}

sealed class TagFilter {
    object All : TagFilter()
    data class Search(val query: String) : TagFilter()
    data class ForLink(val linkId: String) : TagFilter()
}

class ManageTagsUseCase @Inject constructor(
    private val repository: TagRepository
) {
    suspend operator fun invoke(operation: TagOperation): Tag? {
        return when (operation) {
            is TagOperation.Add -> {
                repository.insertTag(operation.tag)
                operation.tag
            }
            is TagOperation.Update -> {
                repository.updateTag(operation.tag)
                operation.tag
            }
            is TagOperation.Delete -> {
                repository.deleteTag(operation.tag)
                operation.tag
            }
            is TagOperation.AddToLink -> {
                repository.addTagToLink(operation.tag, operation.link)
                operation.tag
            }
            is TagOperation.RemoveFromLink -> {
                repository.removeTagFromLink(operation.tag, operation.link)
                operation.tag
            }
            is TagOperation.GetOrCreate -> {
                repository.getOrCreateTag(operation.name)
            }
        }
    }

    fun getTags(filter: TagFilter): Flow<List<Tag>> {
        return when (filter) {
            is TagFilter.All -> repository.getAllTags()
            is TagFilter.Search -> repository.searchTags(filter.query)
            is TagFilter.ForLink -> repository.getTagsForLink(filter.linkId)
        }
    }

    suspend fun isTagExists(name: String): Boolean {
        return repository.isTagExists(name)
    }
}
