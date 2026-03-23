package ru.kyamshanov.missionChat.presentation.models

import ru.kyamshanov.missionChat.domain.models.Identifier

data class TopicPresentationModel(
    val id: Identifier,
    val title: String,
)