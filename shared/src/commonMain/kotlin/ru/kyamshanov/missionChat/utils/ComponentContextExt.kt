package ru.kyamshanov.missionChat.utils

import com.arkivanov.decompose.ComponentContext
import kotlinx.serialization.KSerializer
import pro.respawn.flowmvi.api.*
import pro.respawn.flowmvi.dsl.state
import pro.respawn.flowmvi.essenty.dsl.retainedStore

@OptIn(DelicateStoreApi::class)
inline fun <reified S : MVIState, I : MVIIntent, A : MVIAction> ComponentContext.retainedPersistedStore(
    initial: S,
    persistentKey: String,
    serializer: KSerializer<S>,
    crossinline builder: (S) -> Container<S, I, A>,
): Store<S, I, A> =
    retainedStore<S, I, A>(
        factory = {
            val initial = stateKeeper.consume(persistentKey, serializer) ?: initial
            builder(initial)
        } as () -> Container<S, I, A>
    ).apply {
        stateKeeper.register(
            key = persistentKey,
            strategy = serializer,
        ) { state }
    }
