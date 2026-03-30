package ru.kyamshanov.missionChat.domain.models

import kotlinx.datetime.LocalDateTime

/**
 * Domain model representing a chat entity.
 *
 * @property id Unique identifier of the chat.
 * @property title The display name or title of the chat.
 * @property createdAt The timestamp when the chat was originally created.
 * @property updatedAt The timestamp of the last modification to the chat metadata or content.
 */
data class Chat(
    val id: Identifier,
    val title: String,
    val description: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val headTopic: Topic,
    val isArchived: Boolean,
)