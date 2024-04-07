package com.gitlab.sszuev.flashcards.core.documents

import com.gitlab.sszuev.flashcards.core.documents.xml.LingvoDocumentReader
import com.gitlab.sszuev.flashcards.core.documents.xml.LingvoDocumentWriter

fun createReader(): DocumentReader = LingvoDocumentReader()

fun createWriter(): DocumentWriter = LingvoDocumentWriter()