package com.gitlab.sszuev.flashcards.dbmem

import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.outputStream

internal fun copyClassPathDataToDir(dir: Path) {
    DictionaryStoreTest::class.java.getResourceAsStream("/data")!!.bufferedReader().use { bf ->
        bf.lines().forEach { r ->
            val file = dir.resolve(r).createFile()
            DictionaryStoreTest::class.java.getResourceAsStream("/data/$r")!!.use { src ->
                file.outputStream().use { dst ->
                    src.copyTo(dst)
                }
            }
        }
    }
}