package com.gitlab.sszuev.flashcards.logslib

import org.slf4j.Marker

class SimpleMarker(
    private val name: String,
    private val references: List<Marker> = emptyList()
) : Marker {
    override fun getName(): String = name

    override fun add(reference: Marker) {}

    override fun remove(reference: Marker): Boolean = false

    @Deprecated("Deprecated in Java", ReplaceWith("hasReferences()"))
    override fun hasChildren(): Boolean = hasReferences()

    override fun hasReferences(): Boolean = references.isNotEmpty()

    override fun iterator(): Iterator<Marker> = references.iterator()

    override fun contains(other: Marker): Boolean = references.contains(other)

    override fun contains(name: String): Boolean = references.any { it.name == name }

    override fun toString() = arrayOf(name, *references.toTypedArray()).joinToString(",")
}
