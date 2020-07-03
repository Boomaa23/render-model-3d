package com.boomaa.render3d.math

import java.util.*

abstract class MathBuilder<K, V> {
    val values = LinkedList<K>()

    fun add(input: K): MathBuilder<K, V> {
        this.values.add(input)
        return this
    }

    fun clear(): MathBuilder<K, V> {
        values.clear()
        return this
    }

    abstract fun build(): V
}