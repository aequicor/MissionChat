package ru.kyamshanov.missionChat.presentation.components

import pro.respawn.flowmvi.api.Store
import ru.kyamshanov.missionChat.presentation.contracts.MessagesAction
import ru.kyamshanov.missionChat.presentation.contracts.MessagesIntent
import ru.kyamshanov.missionChat.presentation.contracts.MessagesState

interface MessagesComponent :
    Store<MessagesState, MessagesIntent, MessagesAction>