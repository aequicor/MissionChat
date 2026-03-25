package ru.kyamshanov.missionChat.presentation.models

data class ChatUiModel(
    val id: UiID,
    val title: String,
    val icon: Int,
    val topics: List<TopicUiModel>
)