package ru.kyamshanov.missionChat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.datetime.LocalDateTime
import ru.kyamshanov.missionChat.data.database.entities.MessageEntity
import ru.kyamshanov.missionChat.data.database.entities.TopicEntity
import ru.kyamshanov.missionChat.domain.models.Identifier

/**
 * Data Access Object для работы с темами (topics) в базе данных.
 */
@Dao
interface TopicDao {

    @Query(
        """
        SELECT * FROM topics 
        WHERE chatId = :chatId
        AND (:before IS NULL OR createdAt < :before) 
        AND (:after IS NULL OR createdAt > :after)
        ORDER BY 
        CASE WHEN :isReversed = TRUE THEN updatedAt END DESC, 
        CASE WHEN :isReversed = FALSE THEN updatedAt END ASC 
        LIMIT :limit
        """
    )
    suspend fun getTopics(
        chatId: Identifier,
        limit: Int,
        after: LocalDateTime?,
        before: LocalDateTime?,
        isReversed: Boolean
    ): List<TopicEntity>

    /**
     * Сохраняет новую тему или перезаписывает существующую при совпадении ID.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: TopicEntity)

    /**
     * Удаляет тему по её идентификатору.
     */
    @Query("DELETE FROM topics WHERE id = :topicId")
    suspend fun deleteTopic(topicId: Identifier)

    /**
     * Получает тему по её идентификатору.
     */
    @Query("SELECT * FROM topics WHERE id = :topicId")
    suspend fun getTopicById(topicId: Identifier): TopicEntity
}
