@file:Suppress("MemberVisibilityCanBePrivate")

package com.gitlab.sszuev.flashcards.common.documents.xml

import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList

internal object DOMUtils {
    /**
     * Gets an element by the specified tag or throws an error.
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
        return this.children().mapNotNull { it as? Element }.filter { it.tagName == tag }
    }

    fun Element.children(): Sequence<Node> = this.childNodes.children()

    fun NodeList.children(): Sequence<Node> = sequence {
        (0 until this@children.length).forEach {
            yield(this@children.item(it))
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