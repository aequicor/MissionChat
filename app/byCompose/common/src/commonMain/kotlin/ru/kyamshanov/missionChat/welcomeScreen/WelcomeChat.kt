package ru.kyamshanov.missionChat.welcomeScreen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.kyamshanov.missionChat.ChatInputComponent
import ru.kyamshanov.missionChat.MessagesComponent
import ru.kyamshanov.missionChat.components.WindowScaffold
import ru.kyamshanov.missionChat.components.glassmorphism
import ru.kyamshanov.missionChat.presentation.models.MessagesIntent
import ru.kyamshanov.missionChat.models.MessagesStateUI
import ru.kyamshanov.missionChat.models.subscribeAsUiState
import ru.kyamshanov.missionChat.models.toUI

@Composable
fun WelcomeChat(
    title: String,
    messagesComponent: MessagesComponent,
    chatInputComponent: ChatInputComponent,
    modifier: Modifier = Modifier
) {
    var floatingSlidebarVisibility by remember { mutableStateOf(false) }
    WindowScaffold(
        modifier = modifier.fillMaxSize(),
        slidebarContent = { isVisible ->
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
                exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start)
            ) {
                Box(
                    modifier = Modifier
                        .glassmorphism(
                            shape = RoundedCornerShape(24.dp),
                            backgroundColor = MaterialTheme.colorScheme.surface,
                        )
                        .padding(16.dp)
                ) {
                    WelcomeSlidebar()
                }
            }
        },
        floatingSlidebarContent = {
            AnimatedVisibility(
                visible = it && floatingSlidebarVisibility,
                enter = fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
                exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start)
            ) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    IconButton(
                        onClick = { floatingSlidebarVisibility = false },
                        modifier = Modifier.align(Alignment.Start).padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                    WelcomeSlidebar(modifier.padding(8.dp, 0.dp, 8.dp, 8.dp))
                }
            }
        },
        toolbarContent = {
            Box(
                modifier = Modifier
                    .height(70.dp)
                    .glassmorphism(
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(20.dp),
                    )
            ) {
                HeaderContent(title, it) { floatingSlidebarVisibility = !floatingSlidebarVisibility }
            }
        }) {


        Column {
            Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp)) {
                MessagesSection(messagesComponent)
            }
            Box(
                modifier = Modifier.fillMaxWidth()
                    .glassmorphism(
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                val mState by messagesComponent.subscribeAsUiState { it.toUI() }
                val isGenerating = (mState as? MessagesStateUI.Loaded)?.isGenerating == true

                InputSectionContent(chatInputComponent, isGenerating)
            }
        }
    }
}


@Composable
fun HeaderContent(
    title: String,
    isFloatingSlidebarAvailable: Boolean,
    modifier: Modifier = Modifier,
    clickOnSlidebar: () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxSize().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedContent(
                targetState = !isFloatingSlidebarAvailable,
                transitionSpec = {
                    (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut())
                },
                label = "IconTransform"
            ) { targetChecked ->
                Icon(
                    imageVector = if (targetChecked) Icons.Default.Menu else Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .run {
                            if (targetChecked) this.clickable { clickOnSlidebar() }
                            else this
                        }
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Row {
            IconButton(onClick = {}) {
                Icon(
                    Icons.Default.Search,
                    null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
            IconButton(onClick = {}) {
                Icon(
                    Icons.Default.MoreVert,
                    null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun MessagesSection(
    component: MessagesComponent,
) {
    val state by component.subscribeAsUiState { it.toUI() }
    when (val model = state) {
        is MessagesStateUI.Loaded -> {
            MessagesList(
                messages = model.messages,
                onDelete = { component.intent(MessagesIntent.DeleteMessage(it)) })
        }

        else -> Box(Modifier.fillMaxSize()) {
            CircularProgressIndicator(
                Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
