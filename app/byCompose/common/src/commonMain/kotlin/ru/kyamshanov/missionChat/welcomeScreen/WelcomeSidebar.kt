package ru.kyamshanov.missionChat.welcomeScreen

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.kyamshanov.missionChat.presentation.components.SidebarComponent
import ru.kyamshanov.missionChat.presentation.models.ChatUiModel
import ru.kyamshanov.missionChat.presentation.models.TopicUiModel
import ru.kyamshanov.missionChat.presentation.models.UiID

@Composable
fun WelcomeSidebar(
    sidebarComponent: SidebarComponent,
    modifier: Modifier = Modifier
) {
    // State for interactivity
    var isActiveExpanded by remember { mutableStateOf(true) }
    var isArchiveExpanded by remember { mutableStateOf(false) }

    val sidebarState by sidebarComponent.state.collectAsStateWithLifecycle()
    val selectedChatId = sidebarState.selectedChat?.id

    val activeChats = sidebarState.activeChats
    val archivedChats = sidebarState.archivedChats
    Column(modifier = modifier) {
        // Scrollable Content Area
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Active Chats Section
            item {
                CollapsibleSectionHeader(
                    title = "Active chats",
                    isExpanded = isActiveExpanded,
                    onToggle = { isActiveExpanded = !isActiveExpanded }
                )
            }

            items(activeChats, key = { it.id }) { chat ->
                AnimatedVisibility(
                    visible = isActiveExpanded,
                    enter = fadeIn(animationSpec = tween(200)) + expandVertically(
                        animationSpec = tween(
                            300
                        )
                    ),
                    exit = fadeOut(animationSpec = tween(200)) + shrinkVertically(
                        animationSpec = tween(
                            300
                        )
                    )
                ) {
                    SidebarItem(
                        chat = chat,
                        isSelected = chat.id == selectedChatId,
                        onClick = { sidebarComponent.onSelect(chat, it) },
                        onOpenAllTopics = { /* Handle opening all topics view */ },
                        onArchive = {
                            sidebarComponent.archiveChat(chat)
                        }
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            // Archive Section
            item {
                CollapsibleSectionHeader(
                    title = "Archive",
                    isExpanded = isArchiveExpanded,
                    onToggle = { isArchiveExpanded = !isArchiveExpanded }
                )
            }

            items(archivedChats, key = { it.id }) { chat ->
                AnimatedVisibility(
                    visible = isArchiveExpanded,
                    enter = fadeIn(animationSpec = tween(200)) + expandVertically(
                        animationSpec = tween(
                            300
                        )
                    ),
                    exit = fadeOut(animationSpec = tween(200)) + shrinkVertically(
                        animationSpec = tween(
                            300
                        )
                    )
                ) {
                    SidebarItem(
                        chat = chat,
                        isSelected = chat.id == selectedChatId,
                        onDelete = { sidebarComponent.deleteChat(chat) },
                        onUnarchive = {
                            sidebarComponent.unarchiveChat(chat)
                        }
                    )
                }
            }
        }

        // Profile Section (Fixed at bottom)
        ProfileSection()
    }
}

@Composable
private fun CollapsibleSectionHeader(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val rotation by animateFloatAsState(targetValue = if (isExpanded) 0f else -90f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onToggle)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            fontWeight = FontWeight.SemiBold
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.rotate(rotation)
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SidebarItem(
    chat: ChatUiModel,
    isSelected: Boolean = false,
    onClick: ((topic: TopicUiModel) -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onArchive: (() -> Unit)? = null,
    onUnarchive: (() -> Unit)? = null,
    onOpenAllTopics: (() -> Unit)? = null
) {
    val backgroundColor =
        if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor =
        if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    val fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal

    var isHovered by remember { mutableStateOf(false) }

    // Main Container (Column to hold Chat Row + Topics)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .onPointerEvent(PointerEventType.Enter) { isHovered = true }
            .onPointerEvent(PointerEventType.Exit) { isHovered = false }
    ) {
        // 1. Main Chat Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = contentColor.copy(alpha = if (isSelected) 1f else 0.7f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = chat.title,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                fontWeight = fontWeight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            // Action Buttons (Archive, Unarchive, Delete)
            val showActions =
                isHovered || (isSelected && (onArchive != null || onUnarchive != null))

            AnimatedVisibility(
                visible = showActions,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Row {
                    if (onArchive != null) {
                        IconButton(
                            onClick = onArchive,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Archive,
                                contentDescription = "Archive",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    if (onUnarchive != null) {
                        IconButton(
                            onClick = onUnarchive,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Unarchive,
                                contentDescription = "Unarchive",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    if (onDelete != null) {
                        if (onArchive != null || onUnarchive != null) Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Expanded Topics List (Visible on Hover or if Selected)
        if (chat.topics.isNotEmpty()) {
            AnimatedVisibility(
                visible = isHovered || isSelected,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 4.dp) // Indent topics
                ) {
                    // Show last 3 topics
                    chat.topics.reversed().take(3).forEach { topic ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .let {
                                    if (onClick != null) {
                                        it.clickable { onClick(topic) }
                                    } else {
                                        it
                                    }
                                }
                                .padding(vertical = 6.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Topic,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = topic.title ?: "New topic",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (chat.topics.size > 3) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onOpenAllTopics?.invoke() }
                                .padding(vertical = 6.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "All topics",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileSection() {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF7F50)) // Coral accent
            ) {
                Text(
                    text = "JD",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "John Doe",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Online",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = { /* Handle settings click */ },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
