package ru.kyamshanov.missionChat.di

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import java.util.concurrent.TimeUnit

actual fun createHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient =
    HttpClient(OkHttp) {
        engine {
            // Настраиваем OkHttp для стриминга
            config {
                connectTimeout(30, TimeUnit.SECONDS)
                readTimeout(0, TimeUnit.SECONDS) // Бесконечный read timeout
                writeTimeout(30, TimeUnit.SECONDS)

                // Важно: не буферизировать ответы
                followRedirects(true)
                followSslRedirects(true)
                retryOnConnectionFailure(true)
            }
        }
        block(this)
    }

