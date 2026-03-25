package ru.kyamshanov.missionChat.domain.utils

fun <K, V> Map<K, List<V>>.mix(map: Map<K, List<V>>): LinkedHashMap<K, List<V>> {
    val newMap = LinkedHashMap(this)
    map.forEach { (k, vs) ->
        newMap[k] = getOrDefault(k, emptyList()) + vs
    }
    return newMap
}