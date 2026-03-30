package ru.kyamshanov.missionChat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.datetime.LocalDateTime
import ru.kyamshanov.missionChat.data.database.entities.TopicEntity
import ru.kyamshanov.missionChat.domain.models.Identifier

/**
 * Data Access Object для работы с темами (topics) в базе данных.
 */
@Dao
interface TopicDao {

    /**
     * Получает список тем для конкретного чата.
     * Результаты отсортированы по возрастанию даты создания (новые элементы в конце списка).
     *
     * @param chatId Идентификатор чата, к которому относятся темы.
     * @param limit Максимальное количество возвращаемых тем.
     * @param before Временная метка, до которой должны быть созданы темы.
     * @return Список [TopicEntity].
     */
    @Query("SELECT * FROM topics WHERE chatId = :chatId AND createdAt < :before ORDER BY createdAt LIMIT :limit")
    suspend fun getTopics(chatId: Identifier, limit: Int, before: LocalDateTime): List<TopicEntity>

    /**
     * Получает список тем для конкретного чата.
     * Результаты отсортированы по убыванию даты создания (новые элементы в начале списка).
     *
     * @param chatId Идентификатор чата, к которому относятся темы.
     * @param limit Максимальное количество возвращаемых тем.
     * @param before Временная метка, до которой должны быть созданы темы.
     * @return Список [TopicEntity].
     */
    @Query("SELECT * FROM topics WHERE chatId = :chatId AND createdAt < :before ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getTopicsReversed(chatId: Identifier, limit: Int, before: LocalDateTime): List<TopicEntity>

    /**
     * Сохраняет новую тему или перезаписывает существующую при совпадении ID.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: TopicEntity)

    /**
     * Обновляет данные существующей темы.
     */
    @Update
    suspend fun updateTopic(topic: TopicEntity)

    /**
     * Удаляет тему по её идентификатору.
     */
    @Query("DELETE FROM topics WHERE id = :topicId")
    suspend fun deleteTopic(topicId: Identifier)

    /**
     * Получает тему по её идентификатору.
     */
    @Query("SELECT * FROM topics WHERE id = :topicId")
    suspend fun getTopicById(topicId: Identifier): TopicEntity?
}
