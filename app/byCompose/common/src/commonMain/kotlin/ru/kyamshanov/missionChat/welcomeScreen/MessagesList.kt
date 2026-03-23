@file:OptIn(ExperimentalComposeUiApi::class)

package ru.kyamshanov.missionChat.welcomeScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.m3.Markdown
import kotlinx.collections.immutable.ImmutableList
import ru.kyamshanov.missionChat.components.glassmorphism
import ru.kyamshanov.missionChat.models.MessagesStateUI.MessageModel
import ru.kyamshanov.missionChat.models.MessagesStateUI.MessageModel.MessageType.AI_ASSISTANT
import ru.kyamshanov.missionChat.models.MessagesStateUI.MessageModel.MessageType.Human


@Composable
fun MessagesList(
    messages: ImmutableList<MessageModel>,
    onDelete: (messageId: String) -> Unit
) {
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (listState.firstVisibleItemIndex <= 1) {
            listState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Bottom),
        reverseLayout = true,
    ) {
        items(messages.asReversed(), key = { it.id }) {
            val icon: ImageVector
            val iconDescription: String
            val backgroundColor: Color
            when (it.messageType) {
                Human -> {
                    icon = Icons.Default.Person
                    iconDescription = "Human"
                    backgroundColor = MaterialTheme.colorScheme.surface
                }

                AI_ASSISTANT -> {
                    icon = Icons.AutoMirrored.Filled.Chat
                    iconDescription = "AI Assistant"
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                }

                MessageModel.MessageType.SYSTEM -> TODO()
            }

            ChatCard(
                modifier = Modifier.animateItem(
                    fadeInSpec = tween(300),
                    fadeOutSpec = tween(10),
                    placementSpec = spring(stiffness = Spring.StiffnessLow),
                ),
                icon = icon,
                iconContentDescription = iconDescription,
                title = it.name,
                lastMessage = it.content,
                textColor = MaterialTheme.colorScheme.onSurface,
                backgroundColor = backgroundColor,
                onDelete = { onDelete(it.id) }
            )
        }
    }
}

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