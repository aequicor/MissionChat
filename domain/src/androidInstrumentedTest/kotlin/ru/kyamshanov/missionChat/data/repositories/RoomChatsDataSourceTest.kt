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
import ru.kyamshanov.missionChat.domain.models.Identifier
import ru.kyamshanov.missionChat.domain.models.Topic
import ru.kyamshanov.missionChat.domain.repositories.ChatsDataSource
import ru.kyamshanov.missionChat.domain.utils.now

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33]) // Явно указываем SDK для корректной работы Robolectric в KMP
class RoomChatsDataSourceTest {

    private lateinit var database: AppDatabase
    private lateinit var dataSource: ChatsDataSource

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

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
}
