package com.gitlab.sszuev.flashcards.core.validators

import com.gitlab.sszuev.flashcards.SettingsContext
import com.gitlab.sszuev.flashcards.corlib.ChainDSL
import com.gitlab.sszuev.flashcards.corlib.worker

fun ChainDSL<SettingsContext>.validateSettingsEntity() = worker {
    this.name = "validate settings entity"
    test {
        this.requestSettingsEntity.stageShowNumberOfWords !in 2..50 ||
            this.requestSettingsEntity.numberOfWordsPerStage !in 2..50 ||
            this.requestSettingsEntity.stageOptionsNumberOfVariants !in 2..50
    }
    process {
        fail(validationError(fieldName = this.normalizedRequestAppAuthId.asString(), description = "invalid settings"))
    }
}

