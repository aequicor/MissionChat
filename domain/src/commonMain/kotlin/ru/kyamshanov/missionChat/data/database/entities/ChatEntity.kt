package ru.kyamshanov.missionChat.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.kyamshanov.missionChat.domain.models.Identifier

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: Identifier,
    val title: String,
    val description: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val headTopic: Identifier,
    val isArchived: Boolean,
)
