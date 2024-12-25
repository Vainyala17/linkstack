package com.hp77.linkstash.domain.usecase.link

import com.hp77.linkstash.domain.model.Link
import com.hp77.linkstash.domain.repository.LinkRepository
import javax.inject.Inject

sealed class LinkStateUpdate {
    data class ToggleArchive(val link: Link) : LinkStateUpdate()
    data class ToggleFavorite(val link: Link) : LinkStateUpdate()
    data class SetReminder(val link: Link, val reminderTime: Long) : LinkStateUpdate()
    data class ClearReminder(val link: Link) : LinkStateUpdate()
}

class UpdateLinkStateUseCase @Inject constructor(
    private val repository: LinkRepository
) {
    suspend operator fun invoke(update: LinkStateUpdate) {
        when (update) {
            is LinkStateUpdate.ToggleArchive -> repository.toggleArchive(update.link)
            is LinkStateUpdate.ToggleFavorite -> repository.toggleFavorite(update.link)
            is LinkStateUpdate.SetReminder -> repository.setReminder(update.link, update.reminderTime)
            is LinkStateUpdate.ClearReminder -> repository.clearReminder(update.link)
        }
    }
}
