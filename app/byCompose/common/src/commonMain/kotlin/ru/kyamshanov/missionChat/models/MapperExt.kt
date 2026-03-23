package ru.kyamshanov.missionChat.models

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import pro.respawn.flowmvi.api.ImmutableStore
import pro.respawn.flowmvi.api.MVIAction
import pro.respawn.flowmvi.api.MVIIntent
import pro.respawn.flowmvi.api.MVIState
import pro.respawn.flowmvi.api.SubscriberLifecycle
import pro.respawn.flowmvi.api.SubscriptionMode
import pro.respawn.flowmvi.compose.dsl.DefaultLifecycle
import pro.respawn.flowmvi.compose.dsl.subscribe


/**
 * Подписывается на состояние [ImmutableStore] и преобразует его в [State] с использованием [transformer].
 * Позволяет маппить состояние хранилища в UI-модель с оптимизацией через [derivedStateOf].
 *
 * @param lifecycle Жизненный цикл, в течение которого активна подписка.
 * @param mode Режим подписки (по умолчанию [SubscriptionMode.Started]).
 * @param transformer Функция преобразования внутреннего состояния [S] в результирующий тип [R].
 * @return Объект [State], содержащий преобразованное состояние.
 */
@Composable
inline fun <S : MVIState, I : MVIIntent, A : MVIAction, R> ImmutableStore<S, I, A>.subscribeAsUiState(
    lifecycle: SubscriberLifecycle = DefaultLifecycle,
    mode: SubscriptionMode = SubscriptionMode.Started,
    crossinline transformer: (S) -> R
): State<R> {
    val state by this.subscribe(lifecycle, mode)
    return remember(state) { derivedStateOf { transformer(state) } }
}