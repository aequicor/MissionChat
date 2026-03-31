package ru.kyamshanov.missionChat.domain.models

data class TopicMessages(
    val topic: Topic,
    val messages: List<MessageInference>,
)