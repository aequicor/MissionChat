package ru.kyamshanov.missionChat.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDateTime
import ru.kyamshanov.missionChat.domain.models.Identifier

/**
 * Сущность сообщения для хранения в базе данных.
 *
 * @property id Уникальный идентификатор сообщения.
 * @property topicId Идентификатор темы (топика), к которой относится сообщение.
 * @property type Тип сообщения (например, SYSTEM, HUMAN, ASSISTANT, TOOL).
 * @property content Текстовое содержимое сообщения.
 * @property createdAt Дата и время создания сообщения.
 * @property updatedAt Дата и время последнего обновления сообщения.
 */
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: Identifier,
    val topicId: Identifier,
    val type: String,
    val content: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
