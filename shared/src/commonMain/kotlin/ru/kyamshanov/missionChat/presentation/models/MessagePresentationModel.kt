package ru.kyamshanov.missionChat.presentation.models

import ru.kyamshanov.missionChat.domain.models.Identifier

data class MessagePresentationModel(
    val id: Identifier,
    val content: String,
    val type: MessagePresentationType
)

sealed interface MessagePresentationType {

    data object System : MessagePresentationType

    data object Assistant : MessagePresentationType

    data object Human : MessagePresentationType

}
