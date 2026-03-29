package ru.kyamshanov.missionChat.domain.usecases

import ru.kyamshanov.missionChat.domain.models.ChatsPaginationState

interface GetChatsUseCase {

    suspend fun getActiveChats(): ChatsPaginationState

    suspend fun getArchiveChats(): ChatsPaginationState
}