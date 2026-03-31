package ru.kyamshanov.missionChat.domain.models

typealias ChatsPaginationState = PaginationState<ChatPreview>

typealias TopicsPaginationState = PaginationState<Topic>

typealias MessagesPaginationState = PaginationState<TopicMessages>

data class PaginationState<T>(
    val items: List<T> = emptyList(),
    val hasNext: Boolean = true,
    val hasPrev: Boolean = true,
    val isLoadingNext: Boolean = false,
    val isLoadingPrev: Boolean = false
)