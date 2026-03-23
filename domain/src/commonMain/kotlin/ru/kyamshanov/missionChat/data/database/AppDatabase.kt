package ru.kyamshanov.missionChat.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import ru.kyamshanov.missionChat.data.database.converters.UuidConverter
import ru.kyamshanov.missionChat.data.database.dao.ChatDao
import ru.kyamshanov.missionChat.data.database.dao.MessageDao
import ru.kyamshanov.missionChat.data.database.dao.TopicDao
import ru.kyamshanov.missionChat.data.database.entities.ChatEntity
import ru.kyamshanov.missionChat.data.database.entities.MessageEntity
import ru.kyamshanov.missionChat.data.database.entities.TopicEntity

@Database(entities = [MessageEntity::class, ChatEntity::class, TopicEntity::class], version = 1)
@TypeConverters(UuidConverter::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun chatDao(): ChatDao
    abstract fun topicDao(): TopicDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
