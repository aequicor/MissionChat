package ru.kyamshanov.missionChat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ru.kyamshanov.missionChat.data.database.entities.MessageEntity
import ru.kyamshanov.missionChat.domain.models.Identifier
import kotlinx.datetime.LocalDateTime
import ru.kyamshanov.missionChat.data.database.entities.ChatEntity

/**
 * Data Access Object для работы с сообщениями [MessageEntity] в базе данных.
 */
@Dao
interface MessageDao {

    @Query(
        """
        SELECT * FROM messages 
        WHERE topicId = :topicId
        AND (:before IS NULL OR createdAt < :before) 
        AND (:after IS NULL OR createdAt > :after)
        ORDER BY 
        CASE WHEN :isReversed = TRUE THEN updatedAt END DESC, 
        CASE WHEN :isReversed = FALSE THEN updatedAt END ASC 
        LIMIT :limit
        """
    )
    suspend fun getMessages(
        topicId: Identifier,
        limit: Int,
        after: LocalDateTime?,
        before: LocalDateTime?,
        isReversed: Boolean
    ): List<MessageEntity>

    /**
     * Вставляет новое сообщение. Если сообщение с таким ID уже существует, оно будет перезаписано.
     *
     * @param message Сущность сообщения для вставки.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    /**
     * Удаляет сообщение по его идентификатору.
     *
     * @param messageId Идентификатор сообщения.
     */
    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: Identifier)

    /**
     * Находит сообщение по его идентификатору.
     *
     * @param messageId Идентификатор сообщения.
     * @return [MessageEntity] если сообщение найдено, иначе null.
     */
    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: Identifier): MessageEntity?
}
