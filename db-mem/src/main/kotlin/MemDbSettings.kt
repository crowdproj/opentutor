package com.gitlab.sszuev.flashcards.dbmem

import com.gitlab.sszuev.flashcards.config.BaseConfig
import org.slf4j.LoggerFactory

object MemDbSettings : BaseConfig("/application.properties") {
    private val logger = LoggerFactory.getLogger(MemDbSettings::class.java)

    val dataLocation = getValue(key = "db-mem.data-directory", default = "./db-data")
    val numberOfRightAnswers = getValue(key = "app.tutor.run.answers", default = 10)

    init {
        logger.info(printDetails())
    }

    private fun printDetails(): String {
        return """
            |
            |data-location                  = $dataLocation
            |number-of-right-answers        = $numberOfRightAnswers
            """.replaceIndentByMargin("\t")
    }
}