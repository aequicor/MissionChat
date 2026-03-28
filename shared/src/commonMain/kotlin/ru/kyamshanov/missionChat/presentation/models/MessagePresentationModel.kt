package ru.kyamshanov.missionChat.presentation.models

import kotlinx.serialization.Serializable

@Serializable
data class MessagePresentationModel(
    val id: UiID,
    val content: String,
    val type: MessagePresentationType,
)

@Serializable
sealed interface MessagePresentationType {

    @Serializable
    data object System : MessagePresentationType

    @Serializable
    data object Assistant : MessagePresentationType

    @Serializable
    data object Human : MessagePresentationType

}
