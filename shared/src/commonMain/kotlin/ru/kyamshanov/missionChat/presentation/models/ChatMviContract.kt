package ru.kyamshanov.missionChat.presentation.models

import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import ru.kyamshanov.missionChat.domain.models.Identifier

/**
 * Represents the UI state for the Messages screen.
 */
sealed interface MessagesState : MVIState {

    /**
     * Initial state before any data is loaded.
     */
    data object Idle : MessagesState

    /**
     * State representing an error occurred during message processing or loading.
     * @property e The exception that caused the error state.
     */
    data class Error(val e: Exception?) : MessagesState

    /**
     * State representing successfully loaded messages.
     * @property messages The list of messages to display.
     * @property isGenerating Indicates if an AI response is currently being generated.
     */
    data class Loaded(
        val messages: List<MessagePresentationModel>,
        val isGenerating: Boolean = false,
    ) : MessagesState
}

/**
 * Represents user intentions (actions) on the Messages screen.
 */
sealed interface MessagesIntent : MVIIntent {

    /**
     * Intent to send a new text message.
     * @property message The text content of the message.
     */
    data class SendNewMessage(val message: String) : MessagesIntent

    /**
     * Intent to delete a specific message.
     * @property id The unique identifier of the message to be deleted.
     */
    data class DeleteMessage(val id: Identifier) : MessagesIntent

    /**
     * Intent to stop the current AI generation process.
     */
    data object StopGeneration : MessagesIntent

    /**
     * Intent to load the next page of messages (pagination).
     */
    data object LoadNextMessages : MessagesIntent

}

/**
 * Represents one-time side effects (actions) for the Messages screen.
 */
sealed interface MessagesAction : MVIAction
