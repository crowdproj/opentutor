package com.gitlab.sszuev.flashcards.common.documents.xml

import org.w3c.dom.Element
import org.w3c.dom.Node

internal object DOMUtils {
    /**
     * Gets element by the specified tag or throws an error.
     *
     * @param [tag][String]
     * @return [Element]
     * @throws IllegalStateException if no element found
     */
    fun Element.element(tag: String): Element {
        return elements(tag).single()
    }

    /**
     * Lists elements by tag.
     *
     * @param [tag][String]
     * @return [Sequence] of [Element]s
     */
    fun Element.elements(tag: String): Sequence<Element> {
        return children(this).mapNotNull { it as? Element } .filter { it.tagName == tag }
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