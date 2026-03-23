package ru.kyamshanov.missionChat.models

import androidx.compose.runtime.Immutable

@Immutable
data class ExceptionUI(
    val message: String,
    val original: Throwable? = null
)

fun Exception?.toUI(): ExceptionUI? =
    this?.let { ExceptionUI(it.message.orEmpty(), it) }