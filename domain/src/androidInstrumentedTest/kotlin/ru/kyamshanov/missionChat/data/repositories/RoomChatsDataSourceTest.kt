package ru.kyamshanov.missionChat.data.repositories

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDateTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import ru.kyamshanov.missionChat.data.database.AppDatabase
import ru.kyamshanov.missionChat.data.database.dao.ChatDao
import ru.kyamshanov.missionChat.data.database.dao.MessageDao
import ru.kyamshanov.missionChat.data.database.dao.TopicDao
import ru.kyamshanov.missionChat.data.database.entities.ChatEntity
import ru.kyamshanov.missionChat.data.database.entities.MessageEntity
import ru.kyamshanov.missionChat.data.database.entities.TopicEntity
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.repositories.ChatsDataSource

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class RoomChatsDataSourceTest {

    private val scheduler = TestCoroutineScheduler()
    private val ioDispatcher = StandardTestDispatcher(scheduler)
    private val mainDispatcher = StandardTestDispatcher(scheduler)

    private lateinit var database: AppDatabase
    private lateinit var chatDao: ChatDao
    private lateinit var topicDao: TopicDao
    private lateinit var messageDao: MessageDao
    private lateinit var dataSource: ChatsDataSource

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Dispatchers.setMain(mainDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        chatDao = database.chatDao()
        topicDao = database.topicDao()
        messageDao = database.messageDao()
        dataSource = RoomChatsDataSource(database, ioDispatcher = ioDispatcher)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun test_getChats_variations() = runTest(scheduler) {
        insertTestData()

        // 1. Limit
        val limit1 = dataSource.getChats(limit = 1, isArchived = false, isReversed = false)
        assertEquals("Should return only 1 chat", 1, limit1.size)
        assertEquals("Общий чат", limit1[0].title)

        // 2. isArchived = true
        val archived = dataSource.getChats(limit = 10, isArchived = true, isReversed = false)
        assertEquals("Should return 1 archived chat", 1, archived.size)
        assertEquals("Архив проекта", archived[0].title)
        // 3. isArchived = false
        val active = dataSource.getChats(limit = 10, isArchived = false, isReversed = false)
        assertEquals("Should return 3 active chats", 3, active.size)

        // 4. isReversed = true (Descending by createdAt)
        val reversed = dataSource.getChats(limit = 10, isArchived = false, isReversed = true)
        assertEquals("3-й проект", reversed[0].title)
        assertEquals("Разработка", reversed[1].title)
        assertEquals("Общий чат", reversed[2].title)

        // 5. before filter
        val before = dataSource.getChats(
            limit = 10,
            before = LocalDateTime(2023, 10, 15, 0, 0),
            isArchived = false,
            isReversed = false
        )
        assertEquals(1, before.size)
        assertEquals("Общий чат", before[0].title)

        // 6. after filter
        val after = dataSource.getChats(
            limit = 10,
            after = LocalDateTime(2023, 10, 15, 0, 0),
            isArchived = false,
            isReversed = false
        )
        assertEquals(2, after.size)
        assertEquals("Разработка", after[0].title)
    }

    @Test
    fun test_getTopics_variations() = runTest(scheduler) {
        val (chats, _, _) = insertTestData()
        val chat = dataSource.getChat(chats[0].id)

        // 1. Limit
        val limit2 = dataSource.getTopics(chat.id, limit = 2, isReversed = false)
        assertEquals(2, limit2.size)

        // 2. isReversed = true (Descending by updatedAt)
        val reversed = dataSource.getTopics(chat.id, limit = 10, isReversed = true)
        assertEquals("Предложения", reversed[0].title)
        assertEquals("Правила сообщества", reversed.last().title)

        // 3. after filter
        val after = dataSource.getTopics(
            chatId = chat.id,
            limit = 10,
            after = LocalDateTime(2023, 10, 2, 0, 0),
            isReversed = false
        )
        assertTrue(after.all { it.createdAt > LocalDateTime(2023, 10, 2, 0, 0) })
        assertEquals(3, after.size)
    }

    @Test
    fun test_getMessages_variations() = runTest(scheduler) {
        val (_, topics, _) = insertTestData()
        val topic = dataSource.getTopic(topics[0].id)

        // 1. Limit
        val limit1 = dataSource.getMessages(topic.id, limit = 1, isReversed = false)
        assertEquals(1, limit1.size)
        assertEquals("Hello", limit1[0].text)

        // 2. isReversed = true
        val reversed = dataSource.getMessages(topic.id, limit = 10, isReversed = true)
        assertEquals("How are you?", reversed[0].text)
        assertEquals("Hello", reversed[1].text)

        // 3. before filter
        val before = dataSource.getMessages(
            topic.id,
            limit = 10,
            before = LocalDateTime(2023, 10, 1, 10, 10),
            isReversed = false
        )
        assertEquals(1, before.size)
        assertEquals("Hello", before[0].text)
    }

    private suspend fun insertTestData(): Triple<List<ChatEntity>, List<TopicEntity>, List<MessageEntity>> {
        val (chats, topics, messages) = testData
        chats.forEach { chatDao.insertChat(it) }
        topics.forEach { topicDao.insertTopic(it) }
        messages.forEach { messageDao.insert(it) }
        return Triple(chats, topics, messages)
    }

    companion object {

        private val testData: Triple<List<ChatEntity>, List<TopicEntity>, List<MessageEntity>>
            get() {
                val firstChatId = Identifier.new()
                val firstHeadTopicId = Identifier.new()
                val secondChatId = Identifier.new()
                val secondHeadTopicId = Identifier.new()
                val thirdChatId = Identifier.new()
                val thirdHeadTopicId = Identifier.new()
                val fourthChatId = Identifier.new()
                val fourthHeadTopicId = Identifier.new()

                val chats = listOf(
                    ChatEntity(
                        id = firstChatId,
                        title = "Общий чат",
                        description = "Чат для всех участников",
                        createdAt = LocalDateTime(2023, 10, 1, 10, 0),
                        updatedAt = LocalDateTime(2023, 10, 1, 10, 0),
                        headTopic = firstHeadTopicId,
                        isArchived = false
                    ),
                    ChatEntity(
                        id = secondChatId,
                        title = "Разработка",
                        description = null,
                        createdAt = LocalDateTime(2023, 11, 1, 10, 0),
                        updatedAt = LocalDateTime(2023, 11, 1, 10, 0),
                        headTopic = secondHeadTopicId,
                        isArchived = false
                    ),
                    ChatEntity(
                        id = thirdChatId,
                        title = "3-й проект",
                        description = "Завершенные обсуждения",
                        createdAt = LocalDateTime(2023, 11, 15, 10, 0),
                        updatedAt = LocalDateTime(2023, 11, 15, 10, 0),
                        headTopic = thirdHeadTopicId,
                        isArchived = false
                    ),
                    ChatEntity(
                        id = fourthChatId,
                        title = "Архив проекта",
                        description = "Завершенные обсуждения",
                        createdAt = LocalDateTime(2023, 10, 15, 10, 0),
                        updatedAt = LocalDateTime(2023, 10, 15, 10, 0),
                        headTopic = fourthHeadTopicId,
                        isArchived = true
                    )
                )

                val topics = listOf(
                    TopicEntity(
                        id = firstHeadTopicId,
                        chatId = firstChatId,
                        title = "Правила сообщества",
                        createdAt = LocalDateTime(2023, 10, 1, 10, 5),
                        updatedAt = LocalDateTime(2023, 10, 1, 10, 5)
                    ),
                    TopicEntity(
                        id = Identifier.new(),
                        chatId = firstChatId,
                        title = "Знакомство",
                        createdAt = LocalDateTime(2023, 10, 1, 11, 0),
                        updatedAt = LocalDateTime(2023, 10, 1, 11, 0)
                    ),
                    TopicEntity(
                        id = Identifier.new(),
                        chatId = firstChatId,
                        title = "Частые вопросы",
                        createdAt = LocalDateTime(2023, 10, 2, 9, 0),
                        updatedAt = LocalDateTime(2023, 10, 2, 9, 0)
                    ),
                    TopicEntity(
                        id = Identifier.new(),
                        chatId = firstChatId,
                        title = "Полезные ссылки",
                        createdAt = LocalDateTime(2023, 10, 3, 15, 0),
                        updatedAt = LocalDateTime(2023, 10, 3, 15, 0)
                    ),
                    TopicEntity(
                        id = Identifier.new(),
                        chatId = firstChatId,
                        title = "Предложения",
                        createdAt = LocalDateTime(2023, 10, 4, 10, 0),
                        updatedAt = LocalDateTime(2023, 10, 4, 10, 0)
                    ),
                    TopicEntity(
                        id = secondHeadTopicId,
                        chatId = secondChatId,
                        title = "Обсуждение архитектуры",
                        createdAt = LocalDateTime(2023, 11, 2, 12, 0),
                        updatedAt = LocalDateTime(2023, 11, 3, 15, 30)
                    ),
                    TopicEntity(
                        id = thirdHeadTopicId,
                        chatId = thirdChatId,
                        title = "Обсуждение",
                        createdAt = LocalDateTime(2023, 11, 2, 12, 0),
                        updatedAt = LocalDateTime(2023, 11, 3, 15, 30)
                    ),
                    TopicEntity(
                        id = fourthHeadTopicId,
                        chatId = secondChatId,
                        title = "Обсуждение архитектуры",
                        createdAt = LocalDateTime(2023, 10, 2, 12, 0),
                        updatedAt = LocalDateTime(2023, 10, 3, 15, 30)
                    )
                )

                val messages = listOf(
                    MessageEntity(
                        id = Identifier.new(),
                        topicId = firstHeadTopicId,
                        type = "HUMAN",
                        content = "Hello",
                        createdAt = LocalDateTime(2023, 10, 1, 10, 6),
                        updatedAt = LocalDateTime(2023, 10, 1, 10, 6)
                    ),
                    MessageEntity(
                        id = Identifier.new(),
                        topicId = firstHeadTopicId,
                        type = "ASSISTANT",
                        content = "How are you?",
                        createdAt = LocalDateTime(2023, 10, 1, 10, 15),
                        updatedAt = LocalDateTime(2023, 10, 1, 10, 15)
                    )
                )

                return Triple(chats, topics, messages)
            }
    }
}
