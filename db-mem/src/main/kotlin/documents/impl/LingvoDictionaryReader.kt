package com.gitlab.sszuev.flashcards.dbmem.documents.impl

import com.gitlab.sszuev.flashcards.common.CardStatus
import com.gitlab.sszuev.flashcards.dbmem.dao.*
import com.gitlab.sszuev.flashcards.dbmem.documents.DictionaryReader
import org.w3c.dom.Element
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.IOException
import java.io.InputStream
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

class LingvoDictionaryReader(private val ids: IdSequences) : DictionaryReader {

    override fun parse(input: InputStream): Dictionary {
        return try {
            loadDictionary(InputSource(input))
        } catch (ex: Throwable) {
            throw IllegalStateException(ex)
        }
    }

    @Throws(ParserConfigurationException::class, IOException::class, SAXException::class)
    private fun loadDictionary(source: InputSource): Dictionary {
        val dictionaryId = ids.nextDictionaryId()
        val dbf = DocumentBuilderFactory.newInstance()
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
        val db = dbf.newDocumentBuilder()
        val doc = db.parse(source)
        val root = doc.documentElement
        val src: Language = parseLanguage(root, "sourceLanguageId")
        val dst: Language = parseLanguage(root, "destinationLanguageId")
        return Dictionary(
            id = dictionaryId,
            name = root.getAttribute("title"),
            sourceLanguage = src,
            targetLanguage = dst,
            cards = parseCardList(root, dictionaryId)
        )
    }

    private fun parseCardList(root: Element, dictionaryId: Long): MutableMap<Long, Card> {
        return DOMUtils.elements(root, "card").flatMap { parseMeanings(it, dictionaryId) }
            .associateBy { it.id }.toMutableMap()
    }

    private fun parseMeanings(node: Element, dictionaryId: Long): Sequence<Card> {
        val word: String = DOMUtils.normalizeContent(DOMUtils.getElement(node, "word"))
        return DOMUtils.elements(DOMUtils.getElement(node, "meanings"), "meaning")
            .map { parseMeaning(word, it, dictionaryId) }
    }

    private fun parseMeaning(word: String, node: Element, dictionaryId: Long): Card {
        val cardId = ids.nextCardId()
        val transcription = node.getAttribute("transcription")
        val id = node.getAttribute("partOfSpeech")
        val pos: String? = if (id.isBlank()) null else LingvoMappings.toPartOfSpeechTag(id)
        val statistics = DOMUtils.getElement(node, "statistics")
        val status: CardStatus = LingvoMappings.toStatus(statistics.getAttribute("status"))
        val answered: Int? = if (status != CardStatus.UNKNOWN) {
            statistics.getAttribute("answered").takeIf { it.matches("\\d+".toRegex()) }?.toInt() ?: 0
        } else { // in case of status=4 there is some big number
            null
        }
        val translations = DOMUtils.elements(DOMUtils.getElement(node, "translations"), "word")
            .map { parseTranslation(it, cardId) }.toList()
        val examples = DOMUtils.findElement(node, "examples")
            ?.let { e -> DOMUtils.elements(e, "example").map { parseExample(it, cardId) }.toList() } ?: emptyList()
        return Card(
            id = cardId,
            dictionaryId = dictionaryId,
            text = word,
            transcription = transcription,
            partOfSpeech = pos,
            translations = translations,
            examples = examples,
            answered = answered,
            details = "parsed from lingvo xml",
        )
    }

    private fun parseLanguage(root: Element, id: String): Language {
        return Language(id = LingvoMappings.toLanguageTag(root.getAttribute(id)), partsOfSpeech = "unknown")
    }

    private fun parseTranslation(node: Element, cardId: Long): Translation {
        val id = ids.nextTranslationId()
        return Translation(id = id, text = DOMUtils.normalizeContent(node), cardId = cardId)
    }

    private fun parseExample(node: Element, cardId: Long): Example {
        val id = ids.nextExampleId()
        return Example(id = id, text = DOMUtils.normalizeContent(node), cardId = cardId)
    }
}