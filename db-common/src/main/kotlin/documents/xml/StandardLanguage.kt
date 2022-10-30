package com.gitlab.sszuev.flashcards.common.documents.xml

internal enum class StandardLanguage {
    EN {
        override fun partsOfSpeech(): List<String> {
            return EnPartOfSpeech.values().map { it.term() }
        }
    },
    RU {
        override fun partsOfSpeech(): List<String> {
            return RuPartOfSpeech.values().map { it.term() }
        }
    };

    internal abstract fun partsOfSpeech(): List<String>
}