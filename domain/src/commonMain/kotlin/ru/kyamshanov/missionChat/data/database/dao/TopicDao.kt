package ru.kyamshanov.missionChat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ru.kyamshanov.missionChat.data.database.entities.TopicEntity
import ru.kyamshanov.missionChat.domain.models.Identifier

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics WHERE chatId = :chatId AND createdAt < :before ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getTopics(chatId: Identifier, limit: Int, before: Long): List<TopicEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: TopicEntity)

    @Update
    suspend fun updateTopic(topic: TopicEntity)

    @Query("DELETE FROM topics WHERE id = :topicId")
    suspend fun deleteTopic(topicId: Identifier)

    @Query("SELECT * FROM topics WHERE id = :topicId")
    suspend fun getTopicById(topicId: Identifier): TopicEntity?
}
