package ru.kyamshanov.missionChat.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.kyamshanov.missionChat.domain.models.Identifier

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: Identifier,
    val conversationId: String,
    val type: String, // SYSTEM, HUMAN, ASSISTANT, TOOL
    val content: String,
    val humanName: String? = null,
    val assistantAssociatedHumanName: String? = null,
    val toolId: String? = null,
    val timestamp: Long
)
