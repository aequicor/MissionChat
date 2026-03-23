package ru.kyamshanov.missionChat.presentation.models

import ru.kyamshanov.missionChat.domain.models.MessageInference
import ru.kyamshanov.missionChat.domain.models.Topic

typealias ChatPresentationModel = List<Pair<TopicPresentationModel, List<MessagePresentationModel>>>


fun fromDomain(
    messages: List<Pair<Topic, List<MessageInference>>>
): ChatPresentationModel =
    messages.map { pair ->
        pair.first.toPresentation() to pair.second.map { it.toPresentation() }
    }

fun ChatPresentationModel.copyAndAdd(messages: List<Pair<Topic, List<MessageInference>>>): ChatPresentationModel =