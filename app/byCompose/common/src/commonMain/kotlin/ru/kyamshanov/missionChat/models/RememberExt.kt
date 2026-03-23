package ru.kyamshanov.missionChat.models

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember


/**
 * Запоминает экземпляр компонента в композиции.
 *
 * Используется для инициализации и сохранения объектов (например, ViewModel или Presenter)
 * на протяжении жизненного цикла Composable-функции.
 * @param factory Функция-конструктор для создания компонента.
 */
@Composable
inline fun <T> rememberComponent(crossinline factory: () -> T): T = remember { factory() }