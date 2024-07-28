package com.gitlab.sszuev.flashcards.services

import com.gitlab.sszuev.flashcards.repositories.LanguageRepository

val LANGUAGES by lazy { LanguageRepository.languages().map { Language(code = it.key, name = it.value) } }

data class Language(val code: String, val name: String)