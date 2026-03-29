package ru.kyamshanov.missionChat.domain.models

data class ChatPreview(
    val chat: Chat,
    val firstTopics: List<Topic>
)
