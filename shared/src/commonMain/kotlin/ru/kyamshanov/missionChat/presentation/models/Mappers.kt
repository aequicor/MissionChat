package ru.kyamshanov.missionChat.presentation.models

import ru.kyamshanov.missionChat.domain.models.MessageInference
import ru.kyamshanov.missionChat.domain.models.Topic

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
