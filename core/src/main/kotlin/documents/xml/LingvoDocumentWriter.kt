package com.gitlab.sszuev.flashcards.core.documents.xml

import com.gitlab.sszuev.flashcards.core.documents.DocumentCard
import com.gitlab.sszuev.flashcards.core.documents.DocumentDictionary
import com.gitlab.sszuev.flashcards.core.documents.DocumentWriter
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.OutputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerConfigurationException
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

internal class LingvoDocumentWriter : DocumentWriter {

    override fun write(dictionary: DocumentDictionary, output: OutputStream) {
        val document = toXmlDocument(dictionary)
        val transformer = createXmlTransformer()
        try {
            transformer.transform(DOMSource(document), StreamResult(output))
        } catch (e: TransformerException) {
            throw IllegalStateException(e)
        }
    }

    private fun toXmlDocument(dictionary: DocumentDictionary): Document {
        val res = newXmlDocument()
        val root = res.createElement("dictionary")
        res.appendChild(root)
        root.setAttribute("title", dictionary.name)
        val srcLangId = LingvoMappings.fromLanguageTag(dictionary.sourceLang)
        val dstLangId = LingvoMappings.fromLanguageTag(dictionary.targetLang)
        root.setAttribute("sourceLanguageId", srcLangId)
        root.setAttribute("destinationLanguageId", dstLangId)
        root.setAttribute("targetNamespace", "http://www.abbyy.com/TutorDictionary")
        root.setAttribute("formatVersion", "6")
        root.setAttribute("nextWordId", "42")
        dictionary.cards.forEach { card ->
            val element = res.createElement("card")
            root.appendChild(element)
            writeCard(element, card)
        }
        return res
    }

    private fun writeCard(parent: Element, card: DocumentCard) {
        val doc = parent.ownerDocument
        writeText(parent, "word", card.text)
        val meanings = doc.createElement("meanings")
        parent.appendChild(meanings)
        val meaning = doc.createElement("meaning")
        meanings.appendChild(meaning)
        card.partOfSpeech?.also { meaning.setAttribute("partOfSpeech", LingvoMappings.fromPartOfSpeechTag(it)) }
        card.transcription?.also { meaning.setAttribute("transcription", it) }
        writeMeaningStatistics(meaning, card)
        val translations = doc.createElement("translations")
        meaning.appendChild(translations)
        card.translations.forEach { writeText(translations, "word", it) }
        val examples = doc.createElement("examples")
        meaning.appendChild(examples)
        card.examples.forEach { writeText(examples, "example", it) }
    }

    private fun writeMeaningStatistics(meaning: Element, card: DocumentCard) {
        val doc = meaning.ownerDocument
        val statistics = doc.createElement("statistics")
        meaning.appendChild(statistics)
        statistics.setAttribute("status", LingvoMappings.fromStatus(card.status))
    }

    private fun writeText(parent: Element, tag: String, content: String) {
        val word = parent.ownerDocument.createElement(tag)
        word.textContent = content
        parent.appendChild(word)
    }

    private fun newXmlDocument(): Document {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = try {
            factory.newDocumentBuilder()
        } catch (ex: ParserConfigurationException) {
            throw RuntimeException(ex)
        }
        val res = builder.newDocument()
        res.xmlStandalone = true
        return res
    }

    private fun createXmlTransformer(): Transformer {
        val tf = TransformerFactory.newInstance()
        val transformer = try {
            tf.newTransformer()
        } catch (ex: TransformerConfigurationException) {
            throw IllegalStateException(ex)
        }
        transformer.setOutputProperty(OutputKeys.ENCODING, LingvoMappings.charset.name())
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        return transformer
    }
}