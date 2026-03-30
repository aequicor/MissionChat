package ru.kyamshanov.missionChat.data.database.entities

import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.Topic

inline fun ChatEntity.toDomain(headTopicSupplier: (Identifier) -> Topic): Chat =
    Chat(
        id = id,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt,
        headTopic = headTopicSupplier(headTopic)
    )

fun TopicEntity.toDomain(): Topic =
    Topic(
        id = id,
        chatId = chatId,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt
    )


fun Topic.toEntity() : TopicEntity =
    TopicEntity(
        id = id,
        chatId = chatId,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt
    )