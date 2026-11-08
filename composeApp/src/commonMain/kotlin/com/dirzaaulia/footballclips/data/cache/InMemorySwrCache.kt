package com.dirzaaulia.footballclips.data.cache

import kotlin.time.Clock

class InMemorySwrCache<K, V>(
    private val ttlMillis: Long = 300_000L,
) {
    private data class CacheEntry<V>(
        val data: V,
        val timestamp: Long,
    )

    private val storage = mutableMapOf<K, CacheEntry<V>>()

    fun get(key: K): V? {
        val entry = storage[key] ?: return null
        val now = Clock.System.now().toEpochMilliseconds()
        if (now - entry.timestamp > ttlMillis) {
            return null
        }
        return entry.data
    }

    fun getStale(key: K): V? {
        return storage[key]?.data
    }

    fun isStale(key: K): Boolean {
        val entry = storage[key] ?: return true
        val now = Clock.System.now().toEpochMilliseconds()
        return now - entry.timestamp > ttlMillis
    }

    fun put(key: K, value: V) {
        val now = Clock.System.now().toEpochMilliseconds()
        storage[key] = CacheEntry(
            data = value,
            timestamp = now,
        )
    }

    fun clear() {
        storage.clear()
    }
}
