package com.dirzaaulia.footballclips.data.cache

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InMemorySwrCacheTest {

    @Test
    fun testCachePutAndGet() {
        val cache = InMemorySwrCache<String, String>(ttlMillis = 5_000L)
        assertNull(cache.get("key1"))

        cache.put("key1", "value1")
        assertEquals("value1", cache.get("key1"))
        assertFalse(cache.isStale("key1"))
    }

    @Test
    fun testCacheStaleFallback() {
        val cache = InMemorySwrCache<String, String>(ttlMillis = -1L) // expired immediately
        cache.put("key1", "stale_value")

        // Fresh get returns null because it is expired
        assertNull(cache.get("key1"))
        assertTrue(cache.isStale("key1"))

        // Stale getter retains value for SWR offline fallback
        assertEquals("stale_value", cache.getStale("key1"))
    }

    @Test
    fun testCacheClear() {
        val cache = InMemorySwrCache<String, String>(ttlMillis = 10_000L)
        cache.put("a", "1")
        cache.put("b", "2")
        cache.clear()
        assertNull(cache.get("a"))
        assertNull(cache.get("b"))
    }
}
