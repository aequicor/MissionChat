package ru.kyamshanov.missionChat.welcomeScreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import pro.respawn.flowmvi.compose.dsl.subscribe
import ru.kyamshanov.missionChat.components.WindowScaffold
import ru.kyamshanov.missionChat.components.glassmorphism
import ru.kyamshanov.missionChat.presentation.components.ChatComponent
import ru.kyamshanov.missionChat.presentation.components.SidebarComponent
import ru.kyamshanov.missionChat.presentation.components.WelcomeScreenComponent.ChatContainer
import ru.kyamshanov.missionChat.presentation.contracts.ChatContract

@Composable
fun WelcomeChat(
    title: String,
    messagesComponentProvider: @Composable () -> Value<ChildStack<*, ChatContainer>>,
    chatInputComponent: ChatInputComponent,
    sidebarComponent: SidebarComponent,
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
                    WelcomeSidebar(sidebarComponent)
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
                    WelcomeSidebar(sidebarComponent, modifier.padding(8.dp, 0.dp, 8.dp, 8.dp))
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
                HeaderContent(title, it) {
                    floatingSlidebarVisibility = !floatingSlidebarVisibility
                }
            }
        }) {


        val messagesComponentState by messagesComponentProvider().subscribeAsState()

        Children(
            stack = messagesComponentState,
            animation = stackAnimation(fade()),
        ) {
            Column {
                val messagesComponent = it.instance

                Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp)) {
                    when (messagesComponent) {
                        is ChatContainer.Chat -> {
                            MessagesSection(messagesComponent.component)
                        }

                        is ChatContainer.DummyChat -> {
                            Text("I will be back later")
                        }
                    }

                }
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .glassmorphism(
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    InputSectionContent(chatInputComponent)
                }
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
    component: ChatComponent,
) {
    val state by component.store.subscribe()
    MessagesList(
        topics = state.topics,
        onDelete = { t, m -> component.store.intent(ChatContract.Intent.DeleteMessage(t, m)) })
    /*  is MessagesState.Loaded -> {
          MessagesList(
              topics = model.topics,
              onDelete = { t, m -> component.intent(MessagesIntent.DeleteMessage(t, m)) })
      }

      else -> Box(Modifier.fillMaxSize()) {
          CircularProgressIndicator(
              Modifier.align(Alignment.Center),
              color = MaterialTheme.colorScheme.onSurface
          )
      }*/

}
