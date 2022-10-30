package com.gitlab.sszuev.flashcards.common.documents

import com.gitlab.sszuev.flashcards.common.documents.xml.LingvoDocumentReader
import com.gitlab.sszuev.flashcards.common.documents.xml.LingvoDocumentWriter

fun createReader(ids: IdGenerator): DocumentReader = LingvoDocumentReader(ids)

fun createWriter(): DocumentWriter = LingvoDocumentWriter()