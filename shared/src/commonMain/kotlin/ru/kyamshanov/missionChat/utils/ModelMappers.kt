package ru.kyamshanov.missionChat.utils

import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.MessageInference
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.presentation.models.ChatTopicModel
import ru.kyamshanov.missionChat.presentation.models.ChatUiModel
import ru.kyamshanov.missionChat.presentation.models.MessagePresentationModel
import ru.kyamshanov.missionChat.presentation.models.MessagePresentationType
import ru.kyamshanov.missionChat.presentation.models.TopicUiModel
import ru.kyamshanov.missionChat.presentation.models.toUiID

fun MessageInference.toPresentation(): MessagePresentationModel {
    val type = when (this) {
        is MessageInference.AssistantFunctionCalling -> MessagePresentationType.Assistant
        is MessageInference.AssistantMessage -> MessagePresentationType.Assistant
        is MessageInference.FunctionCallingResponse -> MessagePresentationType.Assistant
        is MessageInference.HumanMessage -> MessagePresentationType.Human
        is MessageInference.SystemMessage -> MessagePresentationType.System
    }
    return MessagePresentationModel(
        id = id.toUiID(),
        content = text,
        type = type
    )
}

fun <K, V> Map<K, List<V>>.add(key: K, value: V): Map<K, List<V>> {
    val list = getOrDefault(key, emptyList()) + value
    return toMutableMap().apply { set(key, list) }
}

fun <K, V> Map<K, List<V>>.set(key: K, index: Int, value: V): Map<K, List<V>> {
    val list = getOrDefault(key, emptyList()).toMutableList().apply { set(index, value) }
    return toMutableMap().apply { set(key, list) }
}

fun Map<Topic, List<MessageInference>>.toTopics(): List<ChatTopicModel> =
    map { (k, vs) ->
        ChatTopicModel(
            topic = k.toUI(),
            messages = vs.map { it.toPresentation() },
        )
    }

fun Map<Chat, List<Topic>>.toUI(): List<ChatUiModel> =
    map { (key, value) ->
        val topics = value.map { it.toUI() }
        val chatUi = key.toUI(topics)
        chatUi
    }

fun Chat.toUI(topics: List<TopicUiModel>): ChatUiModel = ChatUiModel(
    id = id.toUiID(),
    title = title,
    icon = 0,
    topics = topics,
)

fun Topic.toUI(): TopicUiModel = TopicUiModel(
    id = id.toUiID(),
    title = title,
)
