package com.gitlab.sszuev.flashcards.speaker.impl

import com.gitlab.sszuev.flashcards.speaker.TTSSettings
import com.gitlab.sszuev.flashcards.speaker.TextToSpeechService
import java.io.BufferedReader
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.streams.asSequence

class LocalTextToSpeechService(
    internal val resourceIdMapper: (String) -> Pair<String, String>,
    internal val libraries: Map<String, List<ResourceStore>>,
) : TextToSpeechService {

    override fun getResource(id: String, vararg args: String?): ByteArray? {
        val langAndWord = resourceIdMapper(id)
        val lang = langAndWord.first
        val word = langAndWord.second
        val stores = libraries[lang] ?: return null
        return stores.mapNotNull { store ->
            store.getResourcePath(word)?.let { store.getResource(it) } ?: return@mapNotNull null
        }.firstOrNull()
    }

    override fun containsResource(id: String): Boolean {
        val langAndWord = resourceIdMapper(id)
        val lang = langAndWord.first
        val word = langAndWord.second
        val stores = libraries[lang] ?: return false
        return stores.any { it.getResourcePath(word) != null }
    }

    companion object {
        private const val classpathPrefix = "classpath:"

        fun load(
            location: String = TTSSettings.localDataDirectory,
            resourceIdMapper: (String) -> Pair<String, String> = { toResourcePath(it) },
        ): TextToSpeechService {
            val libraries = if (location.startsWith(classpathPrefix)) {
                collectClasspathTarLibraries(location.removePrefix(classpathPrefix))
            } else {
                collectDirectoryTarLibraries(location)
            }
            return LocalTextToSpeechService(resourceIdMapper = resourceIdMapper, libraries = libraries)
        }

        private fun toResourcePath(path: String): Pair<String, String> {
            return path.substringBefore(":") to path.substringAfter(":")
        }

        private fun collectClasspathTarLibraries(classpathRootDir: String): Map<String, List<TarArchiveResourceStore>> {
            return resourceBufferedReader(classpathRootDir).use { rootDirReader ->
                rootDirReader.lines().asSequence().map { it.trim() }
                    .filter { canBeLang(it) }
                    .associateWith { lang ->
                        resourceBufferedReader("$classpathRootDir/$lang").use { langDirReader ->
                            langDirReader.lines().asSequence()
                                .map { it.trim() }
                                .filter { it.endsWith(".tar") }
                                .sorted()
                                .map { "$classpathRootDir/$lang/$it" }
                                .map { TarArchiveResourceStore { resourceInputStream(it) } }.toList()
                        }
                    }
            }
        }

        private fun collectDirectoryTarLibraries(rootDir: String): Map<String, List<TarArchiveResourceStore>> {
            val dir = Paths.get(rootDir).toRealPath()
            check(dir.isDirectory()) {
                "Not a directory: $rootDir"
            }
            return Files.newDirectoryStream(dir).use { d ->
                d.asSequence()
                    .filter { canBeLang(it.fileName.toString()) && it.isDirectory() }
                    .associate { langDir ->
                        langDir.fileName.toString() to Files.newDirectoryStream(langDir).use { file ->
                            file.asSequence()
                                .filter { it.fileName.toString().endsWith(".tar") && it.isRegularFile() }
                                .sortedBy { it.fileName.toString() }
                                .map { TarArchiveResourceStore { it.inputStream() } }.toList()
                        }
                    }
            }
        }

        private fun canBeLang(dir: String): Boolean {
            return dir.length <= 3
        }

        private fun resourceBufferedReader(resource: String): BufferedReader {
            return resourceInputStream(resource).bufferedReader(Charsets.UTF_8)
        }

        private fun resourceInputStream(resource: String): InputStream {
            return requireNotNull(LocalTextToSpeechService::class.java.getResourceAsStream(resource)) {
                "Can't find resource $resource."
            }
        }
    }
}