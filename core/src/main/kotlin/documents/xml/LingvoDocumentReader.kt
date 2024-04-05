package com.gitlab.sszuev.flashcards.core.documents.xml

import com.gitlab.sszuev.flashcards.core.documents.DocumentCard
import com.gitlab.sszuev.flashcards.core.documents.DocumentCardStatus
import com.gitlab.sszuev.flashcards.core.documents.DocumentDictionary
import com.gitlab.sszuev.flashcards.core.documents.DocumentReader
import com.gitlab.sszuev.flashcards.core.documents.xml.DOMUtils.element
import com.gitlab.sszuev.flashcards.core.documents.xml.DOMUtils.elements
import org.w3c.dom.Element
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.IOException
import java.io.InputStream
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

internal class LingvoDocumentReader : DocumentReader {

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

        val srcLang = root.parseLanguage("sourceLanguageId")
        val dstLang = root.parseLanguage("destinationLanguageId")
        return DocumentDictionary(
            name = root.getAttribute("title"),
            sourceLang = srcLang,
            targetLang = dstLang,
            cards = root.parseCardList(),
        )
    }

    private fun Element.parseCardList(): List<DocumentCard> {
        return elements("card").flatMap { it.parseMeanings() }.toList()
    }

    private fun Element.parseMeanings(): Sequence<DocumentCard> {
        val word: String = DOMUtils.normalizeContent(element("word"))
        return element("meanings").elements("meaning").map { it.parseMeaning(word) }
    }

    private fun Element.parseMeaning(word: String): DocumentCard {
        val transcription = getAttribute("transcription").takeIf { it.isNotBlank() }
        val posId = getAttribute("partOfSpeech")
        val pos: String? = if (posId.isBlank()) null else LingvoMappings.toPartOfSpeechTag(posId)
        val statistics = element("statistics")
        val status: DocumentCardStatus = LingvoMappings.toStatus(statistics.getAttribute("status"))
        val translations = element("translations").elements("word")
            .map { DOMUtils.normalizeContent(it) }.toList()
        val examples = elements("examples").singleOrNull()?.elements("example")?.map { DOMUtils.normalizeContent(it) }
            ?.toList()
            ?: emptyList()
        return DocumentCard(
            text = word,
            transcription = transcription,
            partOfSpeech = pos,
            translations = translations,
            examples = examples,
            status = status,
        )
    }

    private fun Element.parseLanguage(id: String): String {
        return LingvoMappings.toLanguageTag(getAttribute(id))
    }
}