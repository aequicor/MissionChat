package ru.kyamshanov.missionChat.presentation.components.internal

import ru.kyamshanov.missionChat.presentation.components.ChatInputComponent
import ru.kyamshanov.missionChat.presentation.contracts.ChatInputContract

internal interface InternalChatInputComponent : ChatInputComponent {

    fun onInternalIntent(internalIntent: ChatInputContract.InternalIntent)
}