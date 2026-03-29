package ru.kyamshanov.missionChat.domain.usecases

import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.Topic

interface GetAllTopicsUseCase {

    suspend fun getTopics(chatId: Identifier): List<Topic>
}