package ru.kyamshanov.missionChat

import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import ru.kyamshanov.missionChat.di.sharedModule


fun initKoin(config: KoinAppDeclaration? = null): Koin =
    startKoin {
        config?.invoke(this)
        modules(sharedModule)
    }.koin
