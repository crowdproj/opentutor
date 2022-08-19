package com.gitlab.sszuev.flashcards.common

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object TutorSettings {
    private val logger = LoggerFactory.getLogger(TutorSettings::class.java)

    private val conf: Config = ConfigFactory.load()

    val numberOfRightAnswers = conf.get(key = "app.tutor.run.answers", default = 10)

    init {
        logger.info(printDetails())
    }

    private fun printDetails(): String {
        return """
            |
            |number-of-right-answers        = $numberOfRightAnswers
            """.replaceIndentByMargin("\t")
    }

    private fun Config.get(key: String, default: Int): Int {
        return if (hasPath(key)) getInt(key) else default
    }

}