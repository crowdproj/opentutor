package com.gitlab.sszuev.flashcards.config

data class RunConfig(val auth: String) {
    companion object {
        val PROD = RunConfig("")
    }
}