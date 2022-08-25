package com.gitlab.sszuev.flashcards.common

enum class CardStatus {
    UNKNOWN, IN_PROCESS, LEARNED
}

fun SysConfig.status(answered: Int?): CardStatus {
    return if (answered == null) {
        CardStatus.UNKNOWN
    } else {
        if (answered >= numberOfRightAnswers) {
            CardStatus.LEARNED
        } else {
            CardStatus.IN_PROCESS
        }
    }
}