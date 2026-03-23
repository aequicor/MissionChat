package ru.kyamshanov.missionChat.models

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.number
import ru.kyamshanov.missionChat.presentation.models.MessagesState
import ru.kyamshanov.missionChat.presentation.models.MessagesState.MessageOwner

sealed interface MessagesStateUI {

    @Immutable
    data class Error(val e: ExceptionUI?) : MessagesStateUI

    @Immutable
    data object Loading : MessagesStateUI

    @Immutable
    data class Loaded(
        val messages: ImmutableList<MessageModel>,
        val isGenerating: Boolean = false,
    ) : MessagesStateUI

    @Immutable
    data class MessageModel(
        val id: String,
        val content: String,
        val messageType: MessageType,
        val date: Date,
        val name: String? = null
    ) {

        enum class MessageType {
            Human, AI_ASSISTANT, SYSTEM
        }

        data class Date(
            val dayOfMonth: Int,
            val month: Int,
            val year: Int
        )
    }
}

fun MessagesState.toUI(): MessagesStateUI = when (this) {
    is MessagesState.Error -> MessagesStateUI.Error(e.toUI())
    is MessagesState.Loaded -> MessagesStateUI.Loaded(
        messages = messages.map { it.toUI() }.toImmutableList(),
        isGenerating = isGenerating
    )
    is MessagesState.Loading -> MessagesStateUI.Loading
}

fun MessagesState.MessageModel.toUI() =
    MessagesStateUI.MessageModel(
        id = id,
        messageType = when (owner) {
            MessageOwner.AI -> MessagesStateUI.MessageModel.MessageType.AI_ASSISTANT
            MessageOwner.Human -> MessagesStateUI.MessageModel.MessageType.Human
            MessageOwner.SYSTEM -> MessagesStateUI.MessageModel.MessageType.SYSTEM
        },
        date = MessagesStateUI.MessageModel.Date(
            dayOfMonth = date.dayOfMonth,
            month = date.month.number,
            year = date.year
        ),
        content = content,
        name = name
    )
