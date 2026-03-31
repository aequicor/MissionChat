package ru.kyamshanov.missionChat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.datetime.LocalDateTime
import ru.kyamshanov.missionChat.data.database.entities.ChatEntity
import ru.kyamshanov.missionChat.domain.models.Identifier

/**
 * Data Access Object для работы с сущностями чатов [ChatEntity].
 */
@Dao
interface ChatDao {


    @Query(
        """
        SELECT * FROM chats 
        WHERE (:before IS NULL OR createdAt < :before) 
        AND (:after IS NULL OR createdAt > :after)
        AND isArchived = :isArchived 
        ORDER BY 
        CASE WHEN :isReversed = TRUE THEN createdAt END DESC, 
        CASE WHEN :isReversed = FALSE THEN createdAt END ASC 
        LIMIT :limit
        """
    )
    suspend fun getChats(
        limit: Int,
        after: LocalDateTime?,
        before: LocalDateTime?,
        isArchived: Boolean,
        isReversed: Boolean
    ): List<ChatEntity>

    /**
     * Сохраняет новый чат или обновляет существующий при совпадении ID.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    /**
     * Обновляет данные существующего чата.
     */
    @Update
    suspend fun updateChat(chat: ChatEntity)

    /**
     * Удаляет чат по его идентификатору.
     */
    @Query("DELETE FROM chats WHERE id = :chatId")
    suspend fun deleteChat(chatId: Identifier)

    /**
     * Получает чат по его идентификатору.
     */
    @Query("SELECT * FROM chats WHERE id = :chatId")
    suspend fun getChatById(chatId: Identifier): ChatEntity
}
