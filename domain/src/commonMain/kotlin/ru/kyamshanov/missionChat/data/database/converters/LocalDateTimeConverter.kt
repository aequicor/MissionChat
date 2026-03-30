package ru.kyamshanov.missionChat.data.database.converters

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDateTime

/**
 * TypeConverter для преобразования [LocalDateTime] в String для Room,
 * чтобы сохранить точность до наносекунд.
 */
object LocalDateTimeConverter {

    /**
     * Преобразует String (ISO-8601) в объект [LocalDateTime].
     */
    @TypeConverter
    fun fromString(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    /**
     * Преобразует объект [LocalDateTime] в String (ISO-8601).
     */
    @TypeConverter
    fun localDateTimeToString(value: LocalDateTime?): String? {
        return value?.toString()
    }
}
