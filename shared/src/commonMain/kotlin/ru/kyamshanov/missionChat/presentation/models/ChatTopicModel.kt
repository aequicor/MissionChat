package ru.kyamshanov.missionChat.presentation.models

data class ChatTopicModel(
    val topic: TopicPresentationModel,
    val messages: List<MessagePresentationModel>
)