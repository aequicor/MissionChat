package ru.kyamshanov.missionChat

import pro.respawn.flowmvi.api.Store
import ru.kyamshanov.missionChat.presentation.models.ChatInputAction
import ru.kyamshanov.missionChat.presentation.models.ChatInputIntent
import ru.kyamshanov.missionChat.presentation.models.ChatInputState

interface ChatInputComponent :
    Store<ChatInputState, ChatInputIntent, ChatInputAction>