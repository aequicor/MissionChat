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

    /**
     * Получает список чатов с фильтрацией по статусу архивации и дате создания.
     * Результаты отсортированы по возрастанию даты создания (новые элементы в конце списка).
     *
     * @param limit Максимальное количество возвращаемых чатов.
     * @param before Временная метка, до которой (не включая) должны быть созданы чаты.
     * @param isArchived Флаг поиска в архивированных или активных чатах.
     * @return Список чатов [ChatEntity].
     */
    @Query("SELECT * FROM chats WHERE createdAt < :before AND isArchived = :isArchived ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getChats(limit: Int, before: LocalDateTime, isArchived : Boolean): List<ChatEntity>

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
    suspend fun getChatById(chatId: Identifier): ChatEntity?
}
