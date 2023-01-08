package com.gitlab.sszuev.flashcards.dbmem.testutils

import com.gitlab.sszuev.flashcards.dbmem.MemDatabase
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.outputStream

internal const val classPathResourceDir = "/db-mem-test-data"

internal fun copyClassPathDataToDir(classPathResourceDir: String, dir: Path) {
    MemDatabase::class.java.getResourceAsStream(classPathResourceDir)!!.bufferedReader().use { bf ->
        bf.lineSequence().forEach { resource ->
            val file = dir.resolve(resource).createFile()
            MemDatabase::class.java.getResourceAsStream("$classPathResourceDir/$resource")!!.use { src ->
                file.outputStream().use { dst ->
                    src.copyTo(dst)
                }
            }
        }
    }
}