package ru.kyamshanov.missionChat.data.database.entities

import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.Interlocutor
import ru.kyamshanov.missionChat.domain.models.MessageInference
import ru.kyamshanov.missionChat.domain.models.ToolEnum
import ru.kyamshanov.missionChat.domain.models.Topic

inline fun ChatEntity.toDomain(headTopicSupplier: (Identifier) -> Topic): Chat =
    Chat(
        id = id,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt,
        headTopic = headTopicSupplier(headTopic),
        description = description,
        isArchived = isArchived
    )

fun TopicEntity.toDomain(): Topic =
    Topic(
        id = id,
        chatId = chatId,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt
    )


fun Topic.toEntity(): TopicEntity =
    TopicEntity(
        id = id,
        chatId = chatId,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun Chat.toEntity(): ChatEntity =
    ChatEntity(
        id = id,
        title = title,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt,
        headTopic = headTopic.id,
        isArchived = isArchived,
    )

fun MessageEntity.toDomain(): MessageInference = when (type) {
    "SYSTEM" -> MessageInference.SystemMessage(
        id = id,
        text = content,
        createdAt = createdAt
    )

    "HUMAN" -> MessageInference.HumanMessage(
        id = id,
        text = content,
        createdAt = createdAt,
        human = Interlocutor.Human(name = "User")
    )

    "ASSISTANT" -> MessageInference.AssistantMessage(
        id = id,
        text = content,
        createdAt = createdAt
    )

    "FUNCTION_CALL" -> MessageInference.AssistantFunctionCalling(
        id = id,
        text = content,
        createdAt = createdAt,
        tool = ToolEnum.valueOf(content.substringBefore(":")),
    )

    "FUNCTION_RESPONSE" -> MessageInference.FunctionCallingResponse(
        id = id,
        text = content,
        createdAt = createdAt,
        tool = ToolEnum.valueOf(content.substringBefore(":"))
    )

    else -> throw IllegalArgumentException("Unknown message type: $type")
}

fun MessageInference.toEntity(topicId: Identifier): MessageEntity {
    val type = when (this) {
        is MessageInference.SystemMessage -> "SYSTEM"
        is MessageInference.HumanMessage -> "HUMAN"
        is MessageInference.AssistantMessage -> "ASSISTANT"
        is MessageInference.AssistantFunctionCalling -> "FUNCTION_CALL"
        is MessageInference.FunctionCallingResponse -> "FUNCTION_RESPONSE"
    }

    return MessageEntity(
        id = id,
        topicId = topicId,
        type = type,
        content = text,
        createdAt = createdAt,
        updatedAt = createdAt
    )
}
