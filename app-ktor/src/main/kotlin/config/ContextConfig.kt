package com.gitlab.sszuev.flashcards.config

import com.gitlab.sszuev.flashcards.AppConfig

data class ContextConfig(val runConfig: RunConfig, val tutorConfig: TutorConfig)

internal fun ContextConfig.toAppConfig() = AppConfig(numberOfRightAnswers = tutorConfig.numberOfRightAnswers)