package ru.kyamshanov.missionChat.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.kyamshanov.missionChat.domain.models.Identifier

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey val id: Identifier,
    val chatId: Identifier,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long
)
