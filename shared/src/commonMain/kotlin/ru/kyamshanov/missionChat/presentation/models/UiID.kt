package ru.kyamshanov.missionChat.presentation.models

import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.toIdentifier

typealias UiID = String

fun Identifier.toUiID(): UiID = toString()

fun UiID.toIdentifier(): Identifier = Identifier.fromString(this)