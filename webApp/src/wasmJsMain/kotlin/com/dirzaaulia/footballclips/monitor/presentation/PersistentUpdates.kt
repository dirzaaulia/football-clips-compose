package com.dirzaaulia.footballclips.monitor.presentation

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.collections.immutable.toPersistentSet

internal fun <K, V> ImmutableMap<K, V>.put(key: K, value: V): ImmutableMap<K, V> = toPersistentMap().put(key, value)
internal fun <K, V> ImmutableMap<K, V>.remove(key: K): ImmutableMap<K, V> = toPersistentMap().remove(key)
internal fun <T> ImmutableSet<T>.add(value: T): ImmutableSet<T> = toPersistentSet().add(value)
internal fun <T> ImmutableSet<T>.remove(value: T): ImmutableSet<T> = toPersistentSet().remove(value)
