package com.gitlab.sszuev.flashcards.core

import com.gitlab.sszuev.flashcards.SettingsContext
import com.gitlab.sszuev.flashcards.core.normalizers.normalizers
import com.gitlab.sszuev.flashcards.core.processes.processGetSettings
import com.gitlab.sszuev.flashcards.core.processes.processUpdateSettings
import com.gitlab.sszuev.flashcards.core.validators.validateSettingsEntity
import com.gitlab.sszuev.flashcards.corlib.chain
import com.gitlab.sszuev.flashcards.model.domain.SettingsOperation

class SettingsCorProcessor {
    suspend fun execute(context: SettingsContext) = businessChain.exec(context)

    companion object {
        private val businessChain = chain {
            name = "Settings Root Chain"
            initContext()

            operation(SettingsOperation.GET_SETTINGS) {
                normalizers(SettingsOperation.GET_SETTINGS)
                validators(SettingsOperation.GET_SETTINGS) { }
                runs(SettingsOperation.GET_SETTINGS) {
                    processGetSettings()
                }
            }

            operation(SettingsOperation.UPDATE_SETTINGS) {
                normalizers(SettingsOperation.UPDATE_SETTINGS)
                validators(SettingsOperation.UPDATE_SETTINGS) {
                    validateSettingsEntity()
                }
                runs(SettingsOperation.UPDATE_SETTINGS) {
                    processUpdateSettings()
                }
            }
        }.build()
    }
}