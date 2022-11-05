package com.gitlab.sszuev.flashcards.common.documents.xml

import com.gitlab.sszuev.flashcards.common.documents.*
import org.w3c.dom.Element
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.IOException
import java.io.InputStream
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

internal class LingvoDocumentReader(private val ids: IdGenerator?) : DocumentReader {

    override fun parse(input: InputStream): DocumentDictionary {
        return try {
            loadDictionary(InputSource(input))
        } catch (ex: Exception) {
            throw IllegalStateException(ex)
        }
    }

    @Throws(ParserConfigurationException::class, IOException::class, SAXException::class)
    private fun loadDictionary(source: InputSource): DocumentDictionary {
        val dbf = DocumentBuilderFactory.newInstance()
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
        val db = dbf.newDocumentBuilder()
        val doc = db.parse(source)
        val root = doc.documentElement

        val dictionaryId = ids?.nextDictionaryId() ?: root.parseId("dictionaryId")
        val srcLang = root.parseLanguage("sourceLanguageId")
        val dstLang = root.parseLanguage("destinationLanguageId")
        return DocumentDictionary(
            id = dictionaryId,
            name = root.getAttribute("title"),
            sourceLang = srcLang,
            targetLang = dstLang,
            cards = root.parseCardList(),
            userId = root.parseId("userId"),
        )
    }

    private fun Element.parseCardList(): List<DocumentCard> {
        return DOMUtils.elements(this, "card").flatMap { it.parseMeanings() }.toList()
    }

    private fun Element.parseMeanings(): Sequence<DocumentCard> {
        val word: String = DOMUtils.normalizeContent(DOMUtils.getElement(this, "word"))
        return DOMUtils.elements(DOMUtils.getElement(this, "meanings"), "meaning")
            .map { it.parseMeaning(word) }
    }

    private fun Element.parseMeaning(word: String): DocumentCard {
        val cardId = ids?.nextCardId() ?: parseId("cardId")
        val transcription = getAttribute("transcription").takeIf { it.isNotBlank() }
        val posId = getAttribute("partOfSpeech")
        val pos: String? = if (posId.isBlank()) null else LingvoMappings.toPartOfSpeechTag(posId)
        val statistics = DOMUtils.getElement(this, "statistics")
        val status: CardStatus = LingvoMappings.toStatus(statistics.getAttribute("status"))
        val answered: Int? = if (status != CardStatus.UNKNOWN) {
            statistics.getAttribute("answered").takeIf { it.matches("\\d+".toRegex()) }?.toInt() ?: 0
        } else { // in case of status=4 there is some big number
            null
        }
        val translations = DOMUtils.elements(DOMUtils.getElement(this, "translations"), "word")
            .map { DOMUtils.normalizeContent(it) }.toList()
        val examples = DOMUtils.findElement(this, "examples")
            ?.let { e -> DOMUtils.elements(e, "example").map { DOMUtils.normalizeContent(it) }.toList() } ?: emptyList()
        return DocumentCard(
            id = cardId,
            text = word,
            transcription = transcription,
            partOfSpeech = pos,
            translations = translations,
            examples = examples,
            answered = answered,
            details = "parsed from lingvo xml",
            status = status,
        )
    }

    private fun Element.parseLanguage(id: String): DocumentLang {
        val tag = LingvoMappings.toLanguageTag(getAttribute(id))
        val pos = LingvoMappings.toPartsOfSpeech(tag)
        return DocumentLang(tag = tag, partsOfSpeech = pos)
    }

    private fun Element.parseId(idAttribute: String): Long? {
        return getAttribute(idAttribute).takeIf { it.isNotBlank() && it.matches("\\d+".toRegex()) }?.toLong()
    }
}