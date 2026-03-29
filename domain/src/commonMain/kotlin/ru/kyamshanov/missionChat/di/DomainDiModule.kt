package ru.kyamshanov.missionChat.di

import io.github.aakira.napier.Napier
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import ru.kyamshanov.missionChat.data.database.AppDatabase
import ru.kyamshanov.missionChat.data.database.createDatabase
import ru.kyamshanov.missionChat.data.network.DeepseekApi
import ru.kyamshanov.missionChat.data.network.DeepseekApiImpl
import ru.kyamshanov.missionChat.data.repositories.RoomChatRepository
import ru.kyamshanov.missionChat.domain.interactors.ChatOrchestrator
import ru.kyamshanov.missionChat.domain.interactors.ChatOrchestratorImpl
import ru.kyamshanov.missionChat.domain.interactors.UserChatInteractor
import ru.kyamshanov.missionChat.domain.interactors.UserChatInteractorImpl
import ru.kyamshanov.missionChat.domain.repositories.ChatRepository
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
val DomainDiModule = module {
    includes(getPlatformModule())
    single { createDatabase(get()) }
    single { get<AppDatabase>().messageDao() }
    single { get<AppDatabase>().chatDao() }
    single { get<AppDatabase>().topicDao() }
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }

    single {
        createHttpClient {
            install(ContentNegotiation) {
                json()
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Napier.d(message, tag = "HTTP Client")
                    }
                }
                level = LogLevel.HEADERS
            }
        }
    }

    factory<DeepseekApi> { DeepseekApiImpl(get(), get()) }
    single<ChatRepository> { RoomChatRepository(get()) }
    single<UserChatInteractor> { UserChatInteractorImpl(get(), get()) }
    single<ChatOrchestrator> { ChatOrchestratorImpl(get()) }
}
