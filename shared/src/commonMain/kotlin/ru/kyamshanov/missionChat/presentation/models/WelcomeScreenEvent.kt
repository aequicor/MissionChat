package ru.kyamshanov.missionChat.presentation.models

internal sealed interface WelcomeScreenEvent {

    data class SendMessage(val msg: String) : WelcomeScreenEvent
    data object StartNewTopic : WelcomeScreenEvent
}