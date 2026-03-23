package ru.kyamshanov.missionChat.data.database.converters

import androidx.room.TypeConverter
import ru.kyamshanov.missionChat.domain.models.Identifier
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
object UuidConverter {
    @TypeConverter
    fun fromString(value: String?): Identifier? {
        return value?.let { Identifier(it) }
    }

    @TypeConverter
    fun toString(uuid: Identifier?): String? {
        return uuid?.toString()
    }
}
