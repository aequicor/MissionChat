package ru.kyamshanov.missionChat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ru.kyamshanov.missionChat.data.database.entities.ChatEntity
import ru.kyamshanov.missionChat.domain.models.Identifier

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats WHERE createdAt < :before ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getChats(limit: Int, before: Long): List<ChatEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Update
    suspend fun updateChat(chat: ChatEntity)

    @Query("DELETE FROM chats WHERE id = :chatId")
    suspend fun deleteChat(chatId: Identifier)

    @Query("SELECT * FROM chats WHERE id = :chatId")
    suspend fun getChatById(chatId: Identifier): ChatEntity?
}
