package com.hp77.linkstash.di

import com.hp77.linkstash.domain.repository.LinkRepository
import com.hp77.linkstash.domain.repository.TagRepository
import com.hp77.linkstash.domain.usecase.link.AddLinkUseCase
import com.hp77.linkstash.domain.usecase.link.GetLinksUseCase
import com.hp77.linkstash.domain.usecase.link.UpdateLinkStateUseCase
import com.hp77.linkstash.domain.usecase.link.UpdateLinkUseCase
import com.hp77.linkstash.domain.usecase.link.ToggleLinkStatusUseCase
import com.hp77.linkstash.domain.usecase.link.ShareToHackerNewsUseCase
import com.hp77.linkstash.domain.usecase.sync.SyncLinksToGitHubUseCase
import com.hp77.linkstash.data.repository.HackerNewsRepository
import com.hp77.linkstash.data.repository.GitHubSyncRepository
import com.hp77.linkstash.domain.usecase.profile.GetGitHubProfileUseCase
import com.hp77.linkstash.domain.usecase.profile.GetHackerNewsProfileUseCase
import com.hp77.linkstash.domain.usecase.tag.ManageTagsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    @ViewModelScoped
    fun provideAddLinkUseCase(
        repository: LinkRepository
    ): AddLinkUseCase = AddLinkUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideGetLinksUseCase(
        repository: LinkRepository
    ): GetLinksUseCase = GetLinksUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideUpdateLinkStateUseCase(
        repository: LinkRepository
    ): UpdateLinkStateUseCase = UpdateLinkStateUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideManageTagsUseCase(
        repository: TagRepository
    ): ManageTagsUseCase = ManageTagsUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideUpdateLinkUseCase(
        repository: LinkRepository
    ): UpdateLinkUseCase = UpdateLinkUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideToggleLinkStatusUseCase(
        repository: LinkRepository
    ): ToggleLinkStatusUseCase = ToggleLinkStatusUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideShareToHackerNewsUseCase(
        hackerNewsRepository: HackerNewsRepository,
        linkRepository: LinkRepository
    ): ShareToHackerNewsUseCase = ShareToHackerNewsUseCase(hackerNewsRepository, linkRepository)

    @Provides
    @ViewModelScoped
    fun provideSyncLinksToGitHubUseCase(
        gitHubSyncRepository: GitHubSyncRepository,
        linkRepository: LinkRepository
    ): SyncLinksToGitHubUseCase = SyncLinksToGitHubUseCase(gitHubSyncRepository, linkRepository)

    @Provides
    @ViewModelScoped
    fun provideGetGitHubProfileUseCase(
        gitHubSyncRepository: GitHubSyncRepository
    ): GetGitHubProfileUseCase = GetGitHubProfileUseCase(gitHubSyncRepository)

    @Provides
    @ViewModelScoped
    fun provideGetHackerNewsProfileUseCase(
        hackerNewsRepository: HackerNewsRepository
    ): GetHackerNewsProfileUseCase = GetHackerNewsProfileUseCase(hackerNewsRepository)
}
