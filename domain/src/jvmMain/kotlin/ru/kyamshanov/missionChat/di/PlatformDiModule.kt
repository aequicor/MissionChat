package ru.kyamshanov.missionChat.di

import org.koin.core.module.Module
import org.koin.dsl.module
import ru.kyamshanov.missionChat.database.getDatabaseBuilder

actual fun getPlatformModule(): Module = module {
    factory { getDatabaseBuilder() }
}