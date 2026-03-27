@file:OptIn(ExperimentalUuidApi::class)

package ru.kyamshanov.missionChat.domain.models

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Domain-level value class representing a unique identifier.
 *
 * Wraps a [String] to provide type safety across the application layers,
 * while maintaining compatibility with [Uuid].
 *
 * @property value The raw string representation of the identifier.
 */
@JvmInline
value class Identifier(
    private val value: String
) {

    /**
     * Creates an [Identifier] from a [Uuid].
     */
    constructor(uuid: Uuid) : this(uuid.toString())

    override fun toString(): String = value

    /**
     * Parses the identifier string into a [Uuid].
     * @throws IllegalArgumentException if the value is not a valid UUID string.
     */
    fun toUuid(): Uuid = Uuid.parse(value)

    companion object {
        /**
         * Generates a new random [Identifier].
         */
        fun new(): Identifier = Identifier(Uuid.random())

        fun fromString(identifier: String): Identifier = Identifier(Uuid.parse(identifier))
    }
}

/**
 * Extension to convert a [Uuid] directly into an [Identifier].
 */
fun Uuid.toIdentifier(): Identifier = Identifier(toString())