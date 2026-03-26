package ru.kyamshanov.missionChat.welcomeScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import ru.kyamshanov.missionChat.presentation.models.MessagePresentationType
import ru.kyamshanov.missionChat.presentation.models.UiID


@Composable
fun MessagesList(
    topics: List<ChatTopicModel>,
    onDelete: (topicId: UiID, messageId: UiID) -> Unit
) {
    val listState = rememberLazyListState()

    val totalItemsCount = remember(topics) { topics.sumOf { it.messages.size + 1 } }
    var isInitialScrollDone by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

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

    Box(modifier = Modifier.fillMaxSize()) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Bottom),
        ) {
            topics.forEachIndexed { topicIndex, (topic, messages) ->
                stickyHeader(key = topic.id) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        FloatingHeader(topic.title, Modifier.align(Alignment.Center))
                    }
                }

                itemsIndexed(messages, key = { index, item -> item.id }) { index, item ->
                    val icon: ImageVector
                    val iconDescription: String
                    val backgroundColor: Color
                    when (item.type) {
                        MessagePresentationType.Human -> {
                            icon = Icons.Default.Person
                            iconDescription = "Human"
                            backgroundColor = MaterialTheme.colorScheme.surface
                        }

                        MessagePresentationType.Assistant -> {
                            icon = Icons.AutoMirrored.Filled.Chat
                            iconDescription = "AI Assistant"
                            backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                        }

                        MessagePresentationType.System -> TODO()
                    }

                    ChatCard(
                        icon = icon,
                        modifier = Modifier.let { modifier ->
                            if (topicIndex == topics.lastIndex && index == messages.lastIndex) {
                                modifier.onSizeChanged {
                                    coroutineScope.launch {
                                        listState.scrollToItem(totalItemsCount - 1)
                                    }
                                }
                            } else {
                                modifier
                            }
                        },
                        iconContentDescription = iconDescription,
                        title = "Xyi",
                        lastMessage = item.content,
                        textColor = MaterialTheme.colorScheme.onSurface,
                        backgroundColor = backgroundColor,
                        onDelete = { onDelete(topic.id, item.id) }
                    )
                }
            }

        }
    }

}

@Composable
fun FloatingHeader(title: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Bottom) {
            Box(
                Modifier.size(42.dp).clip(CircleShape).background(textColor.copy(alpha = 0.1f)),
                Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = iconContentDescription,
                    tint = textColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                if (!title.isNullOrBlank()) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {

                        Text(
                            text = title,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = textColor
                        )
                    }
//                    Text(date, color = textColor.copy(alpha = 0.6f), fontSize = 11.sp)
                }
                SelectionContainer {
                    Markdown(lastMessage)
                }
            }
        }

        AnimatedVisibility(
            visible = isHovered,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
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
}
