package ru.kyamshanov.missionChat.presentation.models

data class ChatTopicModel(
    val topic: TopicUiModel,
    val messages: List<MessagePresentationModel>
)