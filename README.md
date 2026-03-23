# MissionChat 💬

**MissionChat** - это элегантный десктоп клиент для взаимодействия с большими языковыми моделями (LLM), такими как Deepseek.

---

### ✨ Ключевые особенности

*   💻 **Мультиплатформенность**: Полная поддержка macOS, Windows и Linux благодаря **Kotlin Multiplatform**.
*   🚀 **Современный стек**: Создан с использованием передовых технологий, включая **Compose for Desktop** для UI, **Decompose** для архитектуры и **Coroutines** для асинхронности.
*   🎨 **Привлекательный интерфейс**: Чистый, интуитивно понятный и отзывчивый пользовательский интерфейс, который делает общение с ИИ приятным.
*   🧠 **Поддержка LLM**: Легко подключайтесь к различным провайдерам LLM (начиная с Deepseek).
*   📚 **История чатов**: Все ваши диалоги сохраняются локально для быстрого доступа.

---

### 🛠️ Технологический стек

*   [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
*   [Compose for Desktop](https://github.com/JetBrains/compose-jb)
*   [Decompose](https://github.com/arkivanov/Decompose)
*   [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

---

### 🏗️ Структура проекта

Проект имеет модульную структуру для четкого разделения ответственности:

*   `app`: Точка входа для десктопного приложения и платформо-специфичный код.
*   `shared`: Общий модуль Kotlin Multiplatform, содержащий всю бизнес-логику, UI и навигацию с использованием компонентов Decompose:
    *   `RootComponent`: Управляет навигацией между экранами.
    *   `WelcomeScreenComponent`: Экран приветствия.
    *   `MessagesComponent`: Отображает историю сообщений.
    *   `DetailsComponent`: Показывает детали чата.
    *   `ChatInputComponent`: Поле для ввода вашего сообщения.

---

### 🚀 Начало работы

1.  **Клонируйте репозиторий**:
    ```bash
    git clone https://github.com/your-username/MissionChat.git
    ```
2.  **Откройте проект** в `IntelliJ IDEA`.
3.  **Запустите приложение**, выполнив команду в терминале:
    ```bash
    ./gradlew run
    ```

Приятного общения!
