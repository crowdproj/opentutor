package com.gitlab.sszuev.flashcards.common.documents.xml

import org.w3c.dom.Element
import org.w3c.dom.Node

internal object DOMUtils {
    /**
     * Gets element by the specified tag or throws an error.
     *
     * @param [parent][Element]
     * @param [tag][String]
     * @return [Element]
     * @throws IllegalStateException if no element found
     */
    fun getElement(parent: Element, tag: String): Element {
        return elements(parent, tag).single()
    }

    /**
     * Finds element by the specified tag or throws an error.
     *
     * @param [parent][Element]
     * @param [tag][String]
     * @return [Element] or `null`
     * @throws IllegalStateException if there is more than one element found
     */
    fun findElement(parent: Element, tag: String): Element? {
        val list = elements(parent, tag).toList()
        if (list.size == 1) {
            return list[0]
        } else if (list.isEmpty()) {
            return null
        }
        throw IllegalStateException("Expected not more than one member for tag='$tag'")
    }

    /**
     * Lists elements by tag.
     *
     * @param [parent][Element]
     * @param [tag][String]
     * @return [Sequence] of [Element]s
     */
    fun elements(parent: Element, tag: String): Sequence<Element> {
        return children(parent)
            .mapNotNull { it as? Element }
            .filter {
                it.tagName == tag
            }
    }

    /**
     * Lists all direct children of the given element.
     *
     * @param [parent][Element]
     * @return [Sequence] of [Element]s
     */
    private fun children(parent: Element): Sequence<Node> {
        return listChildren(parent).asSequence()
    }

    private fun listChildren(parent: Element): Iterator<Node> {
        val list = parent.childNodes
        val length = list.length
        return object : Iterator<Node> {
            var index = 0
            override fun hasNext(): Boolean {
                return index < length - 1
            }

            override fun next(): Node {
                return list.item(index++) as Node
            }
        }
    }

    /**
     * Returns a text without leading and trailing spaces and new-line symbols.
     *
     * @param [node][Element]
     * @return [String]
     */
    fun normalizeContent(node: Element): String {
        return node.textContent.trim().replace("\\n+$".toRegex(), "").replace("^\\n+".toRegex(), "")
    }
}