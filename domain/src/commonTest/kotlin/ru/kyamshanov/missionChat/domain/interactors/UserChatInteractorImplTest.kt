package ru.kyamshanov.missionChat.domain.interactors

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import ru.kyamshanov.missionChat.data.models.Message
import ru.kyamshanov.missionChat.data.network.DeepseekApi
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.Interlocutor
import ru.kyamshanov.missionChat.domain.models.MessageInference
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.domain.repositories.ChatsDataSource
import ru.kyamshanov.missionChat.domain.utils.now
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserChatInteractorImplTest {

    private lateinit var repository: FakeChatsDataSource
    private lateinit var api: FakeDeepseekApi
    private lateinit var interactor: UserChatInteractorImpl

    @BeforeTest
    fun setup() {
        repository = FakeChatsDataSource()
        api = FakeDeepseekApi()
        interactor = UserChatInteractorImpl(repository, api)
    }

    @Test
    fun `getActiveChats should return non-archived chats`() = runTest {
        val chat1 = createTestChat(isArchived = false)
        val chat2 = createTestChat(isArchived = true)
        repository.saveChat(chat1)
        repository.saveChat(chat2)

        val result = interactor.getActiveChats(10, LocalDateTime.now())

        assertEquals(1, result.size)
        assertEquals(chat1.id, result[0].id)
    }

    @Test
    fun `getArchivedChats should return archived chats`() = runTest {
        val chat1 = createTestChat(isArchived = false)
        val chat2 = createTestChat(isArchived = true)
        repository.saveChat(chat1)
        repository.saveChat(chat2)

        val result = interactor.getArchivedChats(10, LocalDateTime.now())

        assertEquals(1, result.size)
        assertEquals(chat2.id, result[0].id)
    }

    @Test
    fun `createChat should save chat and initial topic`() = runTest {
        val title = "Test Chat"
        val firstTopicTitle = "First Topic"

        val chat = interactor.createChat(title, "Desc", firstTopicTitle)

        assertEquals(title, chat.title)
        assertEquals(firstTopicTitle, chat.headTopic.title)
        assertTrue(repository.chats.containsKey(chat.id))
        assertTrue(repository.topics.containsKey(chat.headTopic.id))
    }

    @Test
    fun `createTopic should save new topic`() = runTest {
        val chatId = Identifier.new()
        val title = "New Topic"

        val topic = interactor.createTopic(chatId, title)

        assertEquals(chatId, topic.chatId)
        assertEquals(title, topic.title)
        assertTrue(repository.topics.containsKey(topic.id))
    }

    @Test
    fun `sendMessage should save human message and assistant response`() = runTest {
        val topic = createTestTopic()
        val humanMessage = MessageInference.HumanMessage(
            id = Identifier.new(),
            text = "Hello",
            createdAt = LocalDateTime.now(),
            human = Interlocutor.Human("User")
        )
        api.streamChunks = listOf("Hi ", "there!")

        val flow = interactor.sendMessage(topic, emptyList(), humanMessage)
        val results = flow.toList()

        assertEquals(2, results.size)
        assertEquals("Hi ", results[0].text)
        assertEquals("Hi there!", results[1].text)

        // Check human message saved
        assertTrue(repository.messages.any { it.second.text == "Hello" })
        // Check assistant message saved (last one)
        assertTrue(repository.messages.any { it.second.text == "Hi there!" })
    }

    @Test
    fun `deleteMessage should call repository`() = runTest {
        val messageId = Identifier.new()
        interactor.deleteMessage(messageId)
        assertEquals(messageId, repository.deletedMessageId)
    }

    @Test
    fun `setArchivationChat should update chat archivation status`() = runTest {
        val chat = createTestChat(isArchived = false)
        interactor.setArchivationChat(chat, true)

        val savedChat = repository.chats.values.first()
        assertTrue(savedChat.isArchived)
    }

    @Test
    fun `getMessages should return messages grouped by topics in chronological order`() = runTest {
        val chatId = Identifier.new()
        // Создаем два топика: старый и новый
        val topic1 = createTestTopic(chatId).copy(createdAt = LocalDateTime(2023, 1, 1, 10, 0))
        val topic2 = createTestTopic(chatId).copy(createdAt = LocalDateTime(2023, 1, 1, 11, 0))
        repository.saveTopic(topic1)
        repository.saveTopic(topic2)

        // Сообщения в первом топике
        val msg1 = createTestHumanMessage(topic1, "Msg 1", LocalDateTime(2023, 1, 1, 10, 5))
        val msg2 = createTestHumanMessage(topic1, "Msg 2", LocalDateTime(2023, 1, 1, 10, 10))
        // Сообщение во втором топике
        val msg3 = createTestHumanMessage(topic2, "Msg 3", LocalDateTime(2023, 1, 1, 11, 5))

        repository.saveMessage(topic1, msg1)
        repository.saveMessage(topic1, msg2)
        repository.saveMessage(topic2, msg3)

        // Запрашиваем сообщения, начиная с последнего топика
        val result = interactor.getMessages(topic2.id, limit = 10)

        assertEquals(2, result.size, "Should contain 2 topics")
        val sortedTopics = result.keys.toList()
        assertEquals(topic1.id, sortedTopics[0].id, "First topic should be the older one")
        assertEquals(topic2.id, sortedTopics[1].id, "Second topic should be the newer one")

        assertEquals(2, result[topic1]?.size)
        assertEquals("Msg 1", result[topic1]?.get(0)?.text)
        assertEquals("Msg 2", result[topic1]?.get(1)?.text)

        assertEquals(1, result[topic2]?.size)
        assertEquals("Msg 3", result[topic2]?.get(0)?.text)
    }

    // --- Helpers and Fakes ---

    private fun createTestChat(isArchived: Boolean): Chat {
        val chatId = Identifier.new()
        val headTopic = createTestTopic(chatId)
        return Chat(
            id = chatId,
            title = "Chat",
            description = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            headTopic = headTopic,
            isArchived = isArchived
        )
    }

    private fun createTestTopic(chatId: Identifier = Identifier.new()) = Topic(
        id = Identifier.new(),
        chatId = chatId,
        title = "Topic",
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    private fun createTestHumanMessage(
        topic: Topic,
        text: String,
        createdAt: LocalDateTime = LocalDateTime.now()
    ): MessageInference.HumanMessage = MessageInference.HumanMessage(
        id = Identifier.new(),
        text = text,
        createdAt = createdAt,
        human = Interlocutor.Human("User")
    )

    private class FakeChatsDataSource : ChatsDataSource {
        val chats = mutableMapOf<Identifier, Chat>()
        val topics = mutableMapOf<Identifier, Topic>()
        val messages = mutableListOf<Pair<Topic, MessageInference>>()
        var deletedMessageId: Identifier? = null

        override suspend fun <R> transaction(block: suspend ChatsDataSource.() -> R): R = block()

        override suspend fun getChats(
            limit: Int,
            after: LocalDateTime?,
            before: LocalDateTime?,
            isArchived: Boolean,
            isReversed: Boolean
        ): List<Chat> {
            var filtered = chats.values.filter { it.isArchived == isArchived }
            if (before != null) filtered = filtered.filter { it.createdAt < before }
            if (after != null) filtered = filtered.filter { it.createdAt > after }
            filtered = if (isReversed) filtered.sortedByDescending { it.createdAt } else filtered.sortedBy { it.createdAt }
            return filtered.take(limit)
        }

        override suspend fun getTopics(
            chatId: Identifier,
            limit: Int,
            after: LocalDateTime?,
            before: LocalDateTime?,
            isArchived: Boolean,
            isReversed: Boolean
        ): List<Topic> {
            var filtered = topics.values.filter { it.chatId == chatId }
            if (before != null) filtered = filtered.filter { it.createdAt < before }
            if (after != null) filtered = filtered.filter { it.createdAt > after }
            filtered = if (isReversed) filtered.sortedByDescending { it.createdAt } else filtered.sortedBy { it.createdAt }
            return filtered.take(limit)
        }

        override suspend fun getMessages(
            topicId: Identifier,
            limit: Int,
            after: LocalDateTime?,
            before: LocalDateTime?,
            isArchived: Boolean,
            isReversed: Boolean
        ): List<MessageInference> {
            var filtered = messages.filter { it.first.id == topicId }.map { it.second }
            if (before != null) filtered = filtered.filter { it.createdAt < before }
            if (after != null) filtered = filtered.filter { it.createdAt > after }
            filtered = if (isReversed) filtered.sortedByDescending { it.createdAt } else filtered.sortedBy { it.createdAt }
            return filtered.take(limit)
        }

        override suspend fun saveChat(chat: Chat) {
            chats[chat.id] = chat
        }

        override suspend fun getChat(chatId: Identifier): Chat = chats[chatId]!!

        override suspend fun deleteChat(chatId: Identifier) {
            chats.remove(chatId)
        }

        override suspend fun saveTopic(topic: Topic) {
            topics[topic.id] = topic
        }

        override suspend fun getTopic(topicId: Identifier): Topic = topics[topicId]!!

        override suspend fun deleteTopic(topicId: Identifier) {
            topics.remove(topicId)
        }

        override suspend fun saveMessage(topic: Topic, message: MessageInference) {
            messages.add(topic to message)
        }

        override suspend fun getMessage(messageId: Identifier): MessageInference =
            messages.first { it.second.id == messageId }.second

        override suspend fun deleteMessage(messageId: Identifier) {
            deletedMessageId = messageId
        }
    }

    private class FakeDeepseekApi : DeepseekApi {
        var streamChunks = listOf<String>()

        override fun chatCompletionStream(
            userMessage: String,
            chatHistory: List<Message>,
            model: String,
            systemPrompt: String,
            temperature: Double,
            maxTokens: Int
        ): Flow<String> = kotlinx.coroutines.flow.flow {
            streamChunks.forEach { emit(it) }
        }

        override suspend fun chatCompletion(
            userMessage: String,
            chatHistory: List<Message>,
            model: String,
            systemPrompt: String,
            temperature: Double,
            maxTokens: Int
        ): String = streamChunks.joinToString("")
    }
}
