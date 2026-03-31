package ru.kyamshanov.missionChat.domain.utils

fun <K, V> Map<K, List<V>>.mix(map: Map<K, List<V>>): LinkedHashMap<K, List<V>> {
    val newMap = LinkedHashMap(this)
    map.forEach { (k, vs) ->
        newMap[k] = getOrDefault(k, emptyList()) + vs
    }
    return newMap
}

fun <K, V> MutableMap<K, V>.putAtIndex(index: Int, key: K, value: V) {
    if (index < 0 || index > this.size) {
        throw IndexOutOfBoundsException("Index: $index, Size: ${this.size}")
    }
    this.remove(key)
    val entries = this.entries.toList()
    this.clear()
    for (i in 0 until index) {
        val entry = entries[i]
        this[entry.key] = entry.value
    }
    this[key] = value
    for (i in index until entries.size) {
        val entry = entries[i]
        this[entry.key] = entry.value
    }
}