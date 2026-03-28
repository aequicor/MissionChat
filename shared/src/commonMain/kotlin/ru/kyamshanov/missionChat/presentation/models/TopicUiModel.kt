package ru.kyamshanov.missionChat.presentation.models

import kotlinx.serialization.Serializable

@Serializable
data class TopicUiModel(
    val id: UiID,
    val title: String?
)
