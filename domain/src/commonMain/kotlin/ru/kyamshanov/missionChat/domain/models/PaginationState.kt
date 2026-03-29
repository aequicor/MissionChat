package ru.kyamshanov.missionChat.domain.models

typealias ChatsPaginationState = PaginationState<ChatPreview>

typealias TopicsPaginationState = PaginationState<Topic>

data class PaginationState<T>(
    val items: List<T> = emptyList(),
    val topCursor: String? = null,
    val bottomCursor: String? = null,
    val hasNext: Boolean = true,
    val hasPrev: Boolean = false,
    val isLoadingNext: Boolean = false,
    val isLoadingPrev: Boolean = false
)