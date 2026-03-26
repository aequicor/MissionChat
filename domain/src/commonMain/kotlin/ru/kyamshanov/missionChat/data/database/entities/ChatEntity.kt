package ru.kyamshanov.missionChat.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDateTime
import ru.kyamshanov.missionChat.domain.models.Identifier

/**
 * Сущность чата для хранения в базе данных.
 *
 * @property id Уникальный идентификатор чата.
 * @property title Заголовок чата.
 * @property description Необязательное описание чата.
 * @property createdAt Дата и время создания чата.
 * @property updatedAt Дата и время последнего обновления чата.
 * @property headTopic Идентификатор основной (последней актуальной) темы в чате.
 * @property isArchived Флаг, указывающий, заархивирован ли чат.
 */
@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: Identifier,
    val title: String,
    val description: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val headTopic: Identifier,
    val isArchived: Boolean,
)
