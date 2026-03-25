package ru.kyamshanov.missionChat.utils

import ru.kyamshanov.missionChat.domain.models.MessageInference
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.presentation.models.MessagePresentationModel
import ru.kyamshanov.missionChat.presentation.models.MessagePresentationType
import ru.kyamshanov.missionChat.presentation.models.TopicPresentationModel

fun MessageInference.toPresentation(): MessagePresentationModel {
    val type = when (this) {
        is MessageInference.AssistantFunctionCalling -> MessagePresentationType.Assistant
        is MessageInference.AssistantMessage -> MessagePresentationType.Assistant
        is MessageInference.FunctionCallingResponse -> MessagePresentationType.Assistant
        is MessageInference.HumanMessage -> MessagePresentationType.Human
        is MessageInference.SystemMessage -> MessagePresentationType.System
    }
    return MessagePresentationModel(
        id = id,
        content = text,
        type = type
    )
}

fun Topic.toPresentation(): TopicPresentationModel =
    TopicPresentationModel(
        id = id,
        title = title,
    )

fun <K, V> Map<K, List<V>>.add(key: K, value: V): Map<K, List<V>> {
    val list = getOrDefault(key, emptyList()) + value
    return toMutableMap().apply { set(key, list) }
}

fun <K, V> Map<K, List<V>>.set(key: K, index: Int, value: V): Map<K, List<V>> {
    val list = getOrDefault(key, emptyList()).toMutableList().apply { set(index, value) }
    return toMutableMap().apply { set(key, list) }
}

fun Map<Topic, List<MessageInference>>.toTopics(): List<ru.kyamshanov.missionChat.presentation.models.ChatTopicModel> =
    map { (k, vs) ->
        ru.kyamshanov.missionChat.presentation.models.ChatTopicModel(
            TopicPresentationModel(
                id = k.id,
                title = k.title
            ),
            vs.map { it.toPresentation() }
        )
    }


