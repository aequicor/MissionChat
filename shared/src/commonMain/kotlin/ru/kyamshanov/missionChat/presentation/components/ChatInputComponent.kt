package ru.kyamshanov.missionChat.presentation.components

import pro.respawn.flowmvi.api.Store
import ru.kyamshanov.missionChat.presentation.contracts.ChatInputAction
import ru.kyamshanov.missionChat.presentation.contracts.ChatInputIntent
import ru.kyamshanov.missionChat.presentation.contracts.ChatInputState

interface ChatInputComponent :
    Store<ChatInputState, ChatInputIntent, ChatInputAction>