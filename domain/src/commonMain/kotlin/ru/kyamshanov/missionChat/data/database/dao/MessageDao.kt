package ru.kyamshanov.missionChat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ru.kyamshanov.missionChat.data.database.entities.MessageEntity
import ru.kyamshanov.missionChat.domain.models.Identifier

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity): Long

    @Update
    suspend fun update(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE conversationId = :topicId AND timestamp < :before ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getMessages(topicId: String, limit: Int, before: Long): List<MessageEntity>

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: Identifier)

    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: Identifier): MessageEntity?
}
