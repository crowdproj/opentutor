package com.gitlab.sszuev.flashcards.common


internal interface PartOfSpeech {
    fun term(): String
}

/**
 * see [wiki: Part of speech](https://en.wikipedia.org/wiki/Part_of_speech)
 * // noun, verb, adjective, adverb, pronoun, preposition, conjunction, interjection, article
 */
internal enum class EnPartOfSpeech : PartOfSpeech {
    NOUN, VERB, ADJECTIVE, ADVERB, PRONOUN, PREPOSITION, CONJUNCTION, INTERJECTION, ARTICLE;

    override fun term(): String {
        return name.lowercase()
    }
}

/**
 * see [wiki: Части речи в русском языке](https://ru.wikipedia.org/wiki/%D0%A7%D0%B0%D1%81%D1%82%D0%B8_%D1%80%D0%B5%D1%87%D0%B8_%D0%B2_%D1%80%D1%83%D1%81%D1%81%D0%BA%D0%BE%D0%BC_%D1%8F%D0%B7%D1%8B%D0%BA%D0%B5)
 */
internal enum class RuPartOfSpeech(private val term: String) : PartOfSpeech {
    NOUN("существительное"),
    ADJECTIVE("прилагательное"),
    NUMERAL("числительное"),
    PRONOUN("местоимение"),
    VERB("глагол"),
    ADVERB("наречие"),
    PARTICIPLE("причастие"),
    PREPOSITION("предлог"),
    CONJUNCTION("союз"),
    PARTICLE("частица"),
    INTERJECTION("междометие"),
    ;

    override fun term(): String {
        return term
    }
}