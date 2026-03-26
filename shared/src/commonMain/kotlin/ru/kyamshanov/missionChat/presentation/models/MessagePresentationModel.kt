package ru.kyamshanov.missionChat.presentation.models

data class MessagePresentationModel(
    val id: UiID,
    val content: String,
    val type: MessagePresentationType,
    val topic: TopicUiModel,
)

sealed interface MessagePresentationType {

    data object System : MessagePresentationType

    data object Assistant : MessagePresentationType

    data object Human : MessagePresentationType

}
