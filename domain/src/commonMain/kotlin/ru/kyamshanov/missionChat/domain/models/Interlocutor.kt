package ru.kyamshanov.missionChat.domain.models


/**
 * Represents a participant in a chat conversation.
 */
sealed interface Interlocutor {

    /**
     * The display name of the interlocutor.
     */
    val name: String?

    /**
     * Represents a human user participating in the chat.
     */
    data class Human(
        override val name: String?
    ) : Interlocutor

    /**
     * Represents an AI or automated assistant participating in the chat.
     */
    data class Assistant(
        override val name: String?
    ) : Interlocutor
}