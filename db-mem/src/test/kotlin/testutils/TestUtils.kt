package com.gitlab.sszuev.flashcards.dbmem.testutils

import com.gitlab.sszuev.flashcards.dbmem.DictionaryStore
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.outputStream

internal const val classPathResourceDir = "/db-mem-test-data"

internal fun copyClassPathDataToDir(classPathResourceDir: String, dir: Path) {
    DictionaryStore::class.java.getResourceAsStream(classPathResourceDir)!!.bufferedReader().use { bf ->
        bf.lines().forEach { r ->
            val file = dir.resolve(r).createFile()
            DictionaryStore::class.java.getResourceAsStream("$classPathResourceDir/$r")!!.use { src ->
                file.outputStream().use { dst ->
                    src.copyTo(dst)
                }
            }
        }
    }
}