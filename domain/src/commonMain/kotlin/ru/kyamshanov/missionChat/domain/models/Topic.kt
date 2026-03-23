package ru.kyamshanov.missionChat.domain.models

import kotlinx.datetime.LocalDateTime


/**
 * Domain model representing a discussion topic or a chat thread.
 *
 * @property id Unique identifier of the topic.
 * @property title The display name or subject of the topic.
 * @property createdAt The timestamp when the topic was initially created.
 * @property updatedAt The timestamp of the last modification to the topic.
 */
data class Topic(
    val id: Identifier,
    val title: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)