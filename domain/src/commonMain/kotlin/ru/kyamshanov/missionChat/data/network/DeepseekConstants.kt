package ru.kyamshanov.missionChat.data.network

internal object DeepseekConstants {
    const val DEFAULT_MODEL = "deepseek-chat"
    const val DEFAULT_SYSTEM_PROMPT = "Ты — полезный ассистент."
    const val ENDPOINT_URL = "https://api.deepseek.com/chat/completions"
    const val ROLE_SYSTEM = "system"
    const val ROLE_USER = "user"
    const val STREAM_DATA_PREFIX = "data: "
    const val STREAM_DONE_MARKER = "[DONE]"
}
