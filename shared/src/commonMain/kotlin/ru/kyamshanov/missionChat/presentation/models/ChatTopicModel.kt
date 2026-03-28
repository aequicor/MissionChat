package ru.kyamshanov.missionChat.presentation.models

import kotlinx.serialization.Serializable

@Serializable
data class ChatTopicModel(
    val topic: TopicUiModel,
    val messages: List<MessagePresentationModel>
)
