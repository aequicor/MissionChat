package ru.kyamshanov.missionChat.welcomeScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.m3.Markdown
import kotlinx.coroutines.launch
import ru.kyamshanov.missionChat.components.glassmorphism
import ru.kyamshanov.missionChat.presentation.models.ChatTopicModel
import ru.kyamshanov.missionChat.presentation.models.MessagePresentationModel
import ru.kyamshanov.missionChat.presentation.models.MessagePresentationType
import ru.kyamshanov.missionChat.presentation.models.UiID

@Composable
fun MessagesList(
    topics: List<ChatTopicModel>,
    selectedTopicId: UiID?,
    onDelete: (topicId: UiID, messageId: UiID) -> Unit
) {
    val listState = rememberLazyListState()
    val totalItemsCount = remember(topics) { topics.sumOf { it.messages.size + 1 } }

    AutoScrollEffect(listState, topics, totalItemsCount)

    Box(modifier = Modifier.fillMaxSize()) {
        MessagesListContent(
            topics = topics,
            listState = listState,
            totalItemsCount = totalItemsCount,
            selectedTopicId = selectedTopicId,
            onDelete = onDelete
        )
    }
}

@Composable
private fun AutoScrollEffect(
    listState: LazyListState,
    topics: List<ChatTopicModel>,
    totalItemsCount: Int
) {
    var isInitialScrollDone by remember { mutableStateOf(false) }

    LaunchedEffect(topics) {
        if (totalItemsCount > 0) {
            if (!isInitialScrollDone) {
                listState.scrollToItem(totalItemsCount - 1)
                isInitialScrollDone = true
            } else {
                val layoutInfo = listState.layoutInfo
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                val isAtBottom = lastVisibleItem != null &&
                        lastVisibleItem.index >= layoutInfo.totalItemsCount - 3

                if (isAtBottom) {
                    listState.animateScrollToItem(totalItemsCount - 1)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessagesListContent(
    topics: List<ChatTopicModel>,
    listState: LazyListState,
    totalItemsCount: Int,
    selectedTopicId: UiID?,
    onDelete: (topicId: UiID, messageId: UiID) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Bottom),
    ) {
        topics.forEachIndexed { topicIndex, topicModel ->
            val isSelected = selectedTopicId == topicModel.topic.id
            stickyHeader(key = topicModel.topic.id) {
                if (isSelected) {
                    SelectedTopicHeader(topicModel.topic.title)
                } else {
                    TopicHeader(topicModel.topic.title)
                }
            }

            itemsIndexed(
                items = topicModel.messages,
                key = { _, item -> item.id }
            ) { index, message ->
                val isLastMessage =
                    topicIndex == topics.lastIndex && index == topicModel.messages.lastIndex

                MessageItem(
                    message = message,
                    modifier = if (isLastMessage) {
                        Modifier.onSizeChanged {
                            coroutineScope.launch {
                                listState.scrollToItem(totalItemsCount - 1)
                            }
                        }
                    } else Modifier,
                    onDelete = { onDelete(topicModel.topic.id, message.id) }
                )
            }
        }
    }
}

@Composable
private fun TopicHeader(title: String?) {
    if (title.isNullOrBlank()) return
    Box(modifier = Modifier.fillMaxWidth()) {
        FloatingHeader(title, Modifier.align(Alignment.Center))
    }
}

@Composable
private fun SelectedTopicHeader(title: String?) {
    if (title.isNullOrBlank()) return
    Box(modifier = Modifier.fillMaxWidth()) {
        FloatingHeader(
            title = title,
            modifier = Modifier.align(Alignment.Center),
            backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun MessageItem(
    message: MessagePresentationModel,
    modifier: Modifier = Modifier,
    onDelete: () -> Unit
) {
    val style = getMessageStyle(message.type)

    ChatCard(
        icon = style.icon,
        iconContentDescription = style.description,
        title = style.title,
        lastMessage = message.content,
        textColor = MaterialTheme.colorScheme.onSurface,
        backgroundColor = style.backgroundColor,
        modifier = modifier,
        onDelete = onDelete
    )
}

@Composable
private fun getMessageStyle(type: MessagePresentationType): MessageStyle = when (type) {
    MessagePresentationType.Human -> MessageStyle(
        icon = Icons.Default.Person,
        description = "Human",
        backgroundColor = MaterialTheme.colorScheme.surface,
        title = "You"
    )

    MessagePresentationType.Assistant -> MessageStyle(
        icon = Icons.AutoMirrored.Filled.Chat,
        description = "AI Assistant",
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        title = "AI Assistant"
    )

    MessagePresentationType.System -> TODO("System message type not implemented")
}

private data class MessageStyle(
    val icon: ImageVector,
    val description: String,
    val backgroundColor: Color,
    val title: String
)

@Composable
fun FloatingHeader(
    title: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ChatCard(
    icon: ImageVector,
    iconContentDescription: String,
    title: String?,
    lastMessage: String,
    textColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onDelete: () -> Unit = {}
) {
    var isHovered by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onPointerEvent(PointerEventType.Enter) { isHovered = true }
            .onPointerEvent(PointerEventType.Exit) { isHovered = false }
            .glassmorphism(
                shape = RoundedCornerShape(16.dp),
                backgroundColor = backgroundColor
            )
    ) {
        MessageCardContent(
            icon = icon,
            iconContentDescription = iconContentDescription,
            title = title,
            message = lastMessage,
            textColor = textColor
        )

        DeleteAction(
            isVisible = isHovered,
            textColor = textColor,
            onDelete = onDelete,
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
        )
    }
}

@Composable
private fun MessageCardContent(
    icon: ImageVector,
    iconContentDescription: String,
    title: String?,
    message: String,
    textColor: Color
) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        MessageIcon(icon, iconContentDescription, textColor)

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.align(Alignment.CenterVertically)) {
            if (!title.isNullOrBlank()) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = textColor
                )
            }
            SelectionContainer {
                Markdown(message)
            }
        }
    }
}

@Composable
private fun MessageIcon(
    icon: ImageVector,
    contentDescription: String,
    tint: Color
) {
    Box(
        Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.1f)),
        Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun DeleteAction(
    isVisible: Boolean,
    textColor: Color,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = textColor.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
