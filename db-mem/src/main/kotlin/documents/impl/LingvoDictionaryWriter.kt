package com.gitlab.sszuev.flashcards.dbmem.documents.impl

import com.gitlab.sszuev.flashcards.dbmem.MemDbConfig
import com.gitlab.sszuev.flashcards.dbmem.dao.Card
import com.gitlab.sszuev.flashcards.dbmem.dao.Dictionary
import com.gitlab.sszuev.flashcards.dbmem.documents.DictionaryWriter
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.OutputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.*
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class LingvoDictionaryWriter(private val config: MemDbConfig) : DictionaryWriter {

    override fun write(dictionary: Dictionary, output: OutputStream) {
        val document = toXmlDocument(dictionary)
        val transformer = createXmlTransformer()
        try {
            transformer.transform(DOMSource(document), StreamResult(output))
        } catch (e: TransformerException) {
            throw IllegalStateException(e)
        }
    }

    private fun toXmlDocument(dictionary: Dictionary): Document {
        val res = newXmlDocument()
        val root = res.createElement("dictionary")
        res.appendChild(root)
        root.setAttribute("title", dictionary.name)
        val src: String = LingvoMappings.fromLanguageTag(dictionary.sourceLanguage.id)
        val dst: String = LingvoMappings.fromLanguageTag(dictionary.targetLanguage.id)
        root.setAttribute("sourceLanguageId", src)
        root.setAttribute("destinationLanguageId", dst)
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

    private fun writeCard(parent: Element, card: Card) {
        val doc = parent.ownerDocument
        writeWord(parent, card.text)
        val meanings = doc.createElement("meanings")
        parent.appendChild(meanings)
        val meaning = doc.createElement("meaning")
        meanings.appendChild(meaning)
        card.partOfSpeech?.also { meaning.setAttribute("partOfSpeech", LingvoMappings.fromPartOfSpeechTag(it)) }
        card.transcription?.also { meaning.setAttribute("transcription", it) }
        writeMeaningStatistics(meaning, card)
        val translations = doc.createElement("translations")
        meaning.appendChild(translations)
        card.translations.forEach { writeWord(translations, it.text) }
        val examples = doc.createElement("examples")
        meaning.appendChild(examples)
        card.examples.forEach { writeText(examples, "example", it.text) }
    }

    private fun writeMeaningStatistics(meaning: Element, card: Card) {
        val doc = meaning.ownerDocument
        val statistics = doc.createElement("statistics")
        meaning.appendChild(statistics)
        val status: Status
        if (card.answered == null) {
            status = Status.UNKNOWN
        } else {
            status = if (card.answered >= config.numberOfRightAnswers) {
                Status.LEARNED
            } else {
                Status.IN_PROCESS
            }
            statistics.setAttribute("answered", card.answered.toString())
        }
        statistics.setAttribute("status", LingvoMappings.fromStatus(status))
    }

    private fun writeWord(parent: Element, txt: String) {
        writeText(parent, "word", txt)
    }

    private fun writeText(parent: Element, tag: String, txt: String) {
        val word = parent.ownerDocument.createElement(tag)
        word.textContent = txt
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
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes")
        return transformer
    }
}