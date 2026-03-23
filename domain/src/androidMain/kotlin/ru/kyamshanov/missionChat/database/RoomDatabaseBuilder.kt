package ru.kyamshanov.missionChat.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.kyamshanov.missionChat.data.database.AppDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val dbFile = context.getDatabasePath("mission_chat.db")
    return Room.databaseBuilder<AppDatabase>(
        context = context,
        name = dbFile.absolutePath
    )
}
