package ru.kyamshanov.missionChat.data.repositories

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import ru.kyamshanov.missionChat.data.database.AppDatabase
import ru.kyamshanov.missionChat.data.database.dao.ChatDao
import ru.kyamshanov.missionChat.data.database.dao.TopicDao
import ru.kyamshanov.missionChat.data.database.entities.ChatEntity
import ru.kyamshanov.missionChat.data.database.entities.TopicEntity
import ru.kyamshanov.missionChat.data.database.entities.toDomain
import ru.kyamshanov.missionChat.domain.models.Chat
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.domain.repositories.ChatsDataSource
import ru.kyamshanov.missionChat.domain.utils.now

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33]) // Явно указываем SDK для корректной работы Robolectric в KMP
class RoomChatsDataSourceTest {

    private lateinit var database: AppDatabase
    private lateinit var chatDao: ChatDao
    private lateinit var topicDao: TopicDao
    private lateinit var dataSource: ChatsDataSource

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        chatDao = database.chatDao()
        topicDao = database.topicDao()
        dataSource = RoomChatsDataSource(database)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun test_save_and_get_topic_by_id() = runTest {
        val mockTopicId = Identifier.new()
        val mockChatId = Identifier.new()
        val expectedTopic = Topic(
            id = mockTopicId,
            chatId = mockChatId,
            title = "Test Topic",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )

        dataSource.saveTopic(expectedTopic)
        val actualTopic = dataSource.getTopic(mockTopicId)

        assertEquals(expectedTopic, actualTopic)
    }

    @Test
    fun test_get_before_chats() = runTest {
        val (chats, _) = insertTestData()

        val expected = chats.map {
            it.toDomain { id -> dataSource.getTopic(id) }
        }.sortedBy { it.createdAt }
        val actual = dataSource.getChats()

        assertEquals(expected, actual)
    }

    private suspend fun insertTestData(): Pair<List<ChatEntity>, List<TopicEntity>> {
        val (chats, topics) = testData
        chats.forEach { chatDao.insertChat(it) }
        topics.forEach { topicDao.insertTopic(it) }
        return chats to topics
    }

    companion object {

        private val testData: Pair<List<ChatEntity>, List<TopicEntity>>
            get() {
                val firstChatId = Identifier.new()
                val firstHeadTopicId = Identifier.new()
                val secondChatId = Identifier.new()
                val secondHeadTopicId = Identifier.new()
                val thirdChatId = Identifier.new()
                val thirdHeadTopicId = Identifier.new()
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
                        title = "Архив проекта",
                        description = "Завершенные обсуждения",
                        createdAt = LocalDateTime(2023, 11, 15, 10, 0),
                        updatedAt = LocalDateTime(2023, 11, 15, 10, 0),
                        headTopic = thirdHeadTopicId,
                        isArchived = false
                    )
                )

                val topics = listOf(
                    // Чат 0: 5 топиков
                    TopicEntity(
                        id = firstHeadTopicId,
                        chatId = chats[0].id,
                        title = "Правила сообщества",
                        createdAt = LocalDateTime(2023, 10, 1, 10, 5),
                        updatedAt = LocalDateTime(2023, 10, 1, 10, 5)
                    ),
                    TopicEntity(
                        id = Identifier.new(),
                        chatId = chats[0].id,
                        title = "Знакомство",
                        createdAt = LocalDateTime(2023, 10, 1, 11, 0),
                        updatedAt = LocalDateTime(2023, 10, 1, 11, 0)
                    ),
                    TopicEntity(
                        id = Identifier.new(),
                        chatId = chats[0].id,
                        title = "Частые вопросы",
                        createdAt = LocalDateTime(2023, 10, 2, 9, 0),
                        updatedAt = LocalDateTime(2023, 10, 2, 9, 0)
                    ),
                    TopicEntity(
                        id = Identifier.new(),
                        chatId = chats[0].id,
                        title = "Полезные ссылки",
                        createdAt = LocalDateTime(2023, 10, 3, 15, 0),
                        updatedAt = LocalDateTime(2023, 10, 3, 15, 0)
                    ),
                    TopicEntity(
                        id = Identifier.new(),
                        chatId = chats[0].id,
                        title = "Предложения",
                        createdAt = LocalDateTime(2023, 10, 4, 10, 0),
                        updatedAt = LocalDateTime(2023, 10, 4, 10, 0)
                    ),

                    // Чат 1: 4 топика
                    TopicEntity(
                        id = secondHeadTopicId,
                        chatId = chats[1].id,
                        title = "Обсуждение архитектуры",
                        createdAt = LocalDateTime(2023, 11, 2, 12, 0),
                        updatedAt = LocalDateTime(2023, 11, 3, 15, 30)
                    ),
                    TopicEntity(
                        id = Identifier.new(),
                        chatId = chats[1].id,
                        title = "План на спринт",
                        createdAt = LocalDateTime(2023, 11, 5, 9, 0),
                        updatedAt = LocalDateTime(2023, 11, 5, 9, 0)
                    ),
                    TopicEntity(
                        id = Identifier.new(),
                        chatId = chats[1].id,
                        title = "Баги",
                        createdAt = LocalDateTime(2023, 11, 6, 14, 0),
                        updatedAt = LocalDateTime(2023, 11, 6, 14, 0)
                    ),
                    TopicEntity(
                        id = Identifier.new(),
                        chatId = chats[1].id,
                        title = "Релиз 1.0",
                        createdAt = LocalDateTime(2023, 11, 10, 11, 0),
                        updatedAt = LocalDateTime(2023, 11, 10, 11, 0)
                    ),
                    TopicEntity(
                        id = thirdHeadTopicId,
                        chatId = chats[2].id,
                        title = "План на спринт",
                        createdAt = LocalDateTime(2023, 11, 16, 9, 0),
                        updatedAt = LocalDateTime(2023, 11, 16, 9, 0)
                    )
                )
                return chats to topics
            }
    }
}
