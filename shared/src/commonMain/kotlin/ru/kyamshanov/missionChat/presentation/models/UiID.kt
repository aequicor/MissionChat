package ru.kyamshanov.missionChat.presentation.models

import ru.kyamshanov.missionChat.domain.models.Identifier

typealias UiID = String

fun Identifier.toUiID(): UiID = toString()