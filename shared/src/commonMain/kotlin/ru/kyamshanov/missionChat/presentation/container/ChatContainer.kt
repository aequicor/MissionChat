package ru.kyamshanov.missionChat.presentation.container

import pro.respawn.flowmvi.api.Container
import pro.respawn.flowmvi.dsl.store
import pro.respawn.flowmvi.plugins.recover
import pro.respawn.flowmvi.plugins.reduce
import ru.kyamshanov.missionChat.domain.interactors.UserChatInteractor
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.presentation.models.MessagesAction
import ru.kyamshanov.missionChat.presentation.models.MessagesIntent
import ru.kyamshanov.missionChat.presentation.models.MessagesState

internal class ChatContainer(
    private val chat: Chat,
    private val userChatInteractor: UserChatInteractor
) : Container<MessagesState, MessagesIntent, MessagesAction> {


    override val store = store(initial = MessagesState.Idle) {
        configure {
            debuggable = true
            name = "MessagesContainer"
        }


        recover {
            updateState { MessagesState.Error(it) }
            null
        }

        reduce { intent ->
            when (intent) {
                is MessagesIntent.DeleteMessage -> TODO()
                MessagesIntent.LoadNextMessages -> TODO()
                is MessagesIntent.SendNewMessage -> TODO()
                MessagesIntent.StopGeneration -> TODO()
            }
        }
    }
}
