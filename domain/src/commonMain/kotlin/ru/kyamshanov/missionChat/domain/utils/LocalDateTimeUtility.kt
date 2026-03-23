@file:OptIn(ExperimentalTime::class)

package ru.kyamshanov.missionChat.domain.utils

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Returns the current number of milliseconds since the epoch (1970-01-01T00:00:00Z) from the system clock.
 * @return The current epoch time in milliseconds.
 */
val LocalDateTime.Companion.nowEpochMilliseconds: Long
    get() = Clock.System.now().toEpochMilliseconds()

/**
 * Returns the current date and time from the system clock in the default time zone.
 * @param timeZone The time zone to use for the current date and time.
 * @return [LocalDateTime] representing the current moment.
 */
fun LocalDateTime.Companion.now(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDateTime =
    Clock.System.now().toLocalDateTime(timeZone)


/**
 * Converts this [LocalDateTime] to the number of milliseconds since the epoch.
 * @param timeZone The time zone used to interpret this local date and time.
 * @return The number of milliseconds since 1970-01-01T00:00:00Z.
 */
fun LocalDateTime.toEpochMilliseconds(timeZone: TimeZone = TimeZone.currentSystemDefault()): Long =
    toInstant(timeZone).toEpochMilliseconds()


/**
 * Converts this [Long] (representing milliseconds since the epoch) to a [LocalDateTime]
 * in the specified time zone.
 * @param timeZone The time zone to use for the conversion.
 * @return [LocalDateTime] corresponding to the given epoch milliseconds.
 */
fun Long.toLocalDateTime(timeZone: TimeZone = TimeZone.currentSystemDefault()) =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(timeZone)