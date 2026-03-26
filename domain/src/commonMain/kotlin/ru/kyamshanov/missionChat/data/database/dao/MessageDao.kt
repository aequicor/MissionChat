package ru.kyamshanov.missionChat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ru.kyamshanov.missionChat.data.database.entities.MessageEntity
import ru.kyamshanov.missionChat.domain.models.Identifier
import kotlinx.datetime.LocalDateTime

/**
 * Data Access Object для работы с сообщениями [MessageEntity] в базе данных.
 */
@Dao
interface MessageDao {

    /**
     * Вставляет новое сообщение. Если сообщение с таким ID уже существует, оно будет перезаписано.
     *
     * @param message Сущность сообщения для вставки.
     * @return Row ID вставленной записи.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity): Long

    /**
     * Обновляет данные существующего сообщения.
     *
     * @param message Сущность сообщения с обновленными данными.
     */
    @Update
    suspend fun update(message: MessageEntity)

    /**
     * Получает список сообщений для указанной темы, обновленных до указанной временной метки.
     * Результаты отсортированы по времени последнего обновления в порядке возрастания (старые обновления в начале списка).
     *
     * @param topicId Идентификатор темы.
     * @param limit Максимальное количество сообщений для получения.
     * @param before Временная метка (updatedAt), до которой нужно искать сообщения.
     * @return Список [MessageEntity], отсортированный по возрастанию [MessageEntity.updatedAt].
     */
    @Query("SELECT * FROM messages WHERE topicId = :topicId AND updatedAt < :before ORDER BY updatedAt ASC LIMIT :limit")
    suspend fun getMessages(topicId: Identifier, limit: Int, before: LocalDateTime): List<MessageEntity>

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
