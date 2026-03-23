package ru.kyamshanov.missionChat.database

import androidx.room.Room
import androidx.room.RoomDatabase
import ru.kyamshanov.missionChat.data.database.AppDatabase
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(System.getProperty("user.dir") + "/app/database", "mission_chat.db")
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
    )
}
