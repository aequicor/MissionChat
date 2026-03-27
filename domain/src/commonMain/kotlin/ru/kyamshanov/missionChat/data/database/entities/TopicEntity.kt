package ru.kyamshanov.missionChat.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDateTime
import ru.kyamshanov.missionChat.domain.models.Identifier

/**
 * Сущность темы (топика) для хранения в базе данных.
 *
 * @property id Уникальный идентификатор темы.
 * @property chatId Идентификатор чата, к которому привязана тема.
 * @property title Название темы.
 * @property createdAt Дата и время создания темы.
 * @property updatedAt Дата и время последнего обновления темы.
 */
@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey val id: Identifier,
    val chatId: Identifier,
    val title: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
