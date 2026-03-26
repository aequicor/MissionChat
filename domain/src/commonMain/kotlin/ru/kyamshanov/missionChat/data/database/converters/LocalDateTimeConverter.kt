package ru.kyamshanov.missionChat.data.database.converters

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDateTime
import ru.kyamshanov.missionChat.domain.utils.toEpochMilliseconds
import ru.kyamshanov.missionChat.domain.utils.toLocalDateTime

/**
 * TypeConverter для преобразования [LocalDateTime] в формат Long (timestamp) для Room.
 */
object LocalDateTimeConverter {

    /**
     * Преобразует Long (timestamp) в объект [LocalDateTime].
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.toLocalDateTime()
    }

    /**
     * Преобразует объект [LocalDateTime] в Long (timestamp).
     */
    @TypeConverter
    fun localDateTimeToTimestamp(value: LocalDateTime?): Long? {
        return value?.toEpochMilliseconds()
    }
}
