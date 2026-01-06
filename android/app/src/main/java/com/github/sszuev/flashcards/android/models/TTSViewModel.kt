package com.github.sszuev.flashcards.android.models

import android.app.Application
import android.util.Log
import android.util.LruCache
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.github.sszuev.flashcards.android.AUDIO_PROCESSING_MAX_DELAY_MS
import com.github.sszuev.flashcards.android.repositories.ApiResponseException
import com.github.sszuev.flashcards.android.repositories.InvalidTokenException
import com.github.sszuev.flashcards.android.repositories.TTSRepository
import com.github.sszuev.flashcards.android.utils.langFromAudioResource
import com.github.sszuev.flashcards.android.utils.wordFromAudioResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream

class TTSViewModel(
    private val context: Application,
    private val ttsRepository: TTSRepository,
    private val signOut: () -> Unit,
) : ViewModel() {
    private val tag = "TTSViewModel"

    private val _isAudioLoading = mutableStateMapOf<String, Boolean>()
    private val _isAudioPlaying = mutableStateMapOf<String, Boolean>()
    private val activeMediaPlayers = mutableMapOf<String, ExoPlayer>()
    private val _errorMessage = mutableStateOf<String?>(null)

    private val _audioResources = object : LruCache<String, ByteArray?>(1024) {
        private val NULL_AUDIO_MARKER = ByteArray(0)

        @Synchronized
        fun getNullable(key: String): ByteArray? {
            return when (val result = get(key)) {
                NULL_AUDIO_MARKER -> null
                else -> result
            }
        }

        @Synchronized
        fun putNullable(key: String, value: ByteArray?) {
            put(key, value ?: NULL_AUDIO_MARKER)
        }

        @Synchronized
        fun hasKey(key: String): Boolean {
            return super.get(key) != null
        }

        @Suppress("ReplaceArrayEqualityOpWithArraysEquals")
        @Synchronized
        fun hasKeyAndValue(key: String): Boolean {
            val res = super.get(key)
            return res != null && res != NULL_AUDIO_MARKER
        }
    }

    fun invalidate(audioId: String) {
        _audioResources.remove(audioId)
    }

    fun loadAndPlayAudio(audioId: String) {
        if (isAudioPlaying(audioId)) {
            Log.d(
                tag,
                "loadAndPlayAudion: audio is already playing for [audioId = $audioId]"
            )
            return
        }
        if (isAudioLoaded(audioId)) {
            playAudio(audioId)
        } else {
            loadAudio(audioId = audioId, audioResourceId = audioId) {
                playAudio(audioId)
            }
        }
    }

    private fun loadAudio(audioId: String, audioResourceId: String, onLoaded: () -> Unit) {
        if (_audioResources.hasKeyAndValue(audioId)) {
            onLoaded()
            return
        }
        viewModelScope.launch {
            _loadAudio(audioId, audioResourceId, onLoaded)
        }
    }

    @Suppress("FunctionName")
    private suspend fun _loadAudio(audioId: String, audioResourceId: String, onLoaded: () -> Unit) {
        Log.d(tag, "load audio for card = $audioId")
        _isAudioLoading[audioId] = true
        _errorMessage.value = null
        val lang = langFromAudioResource(audioResourceId)
        val word = wordFromAudioResource(audioResourceId)
        try {
            val resource = withContext(Dispatchers.IO) {
                ttsRepository.get(lang, word)
            }
            _audioResources.putNullable(audioId, resource)
            if (resource != null) {
                onLoaded()
            }
        } catch (_: InvalidTokenException) {
            signOut()
        } catch (e: ApiResponseException) {
            _audioResources.putNullable(audioId, null)
            Log.e(tag, "Failed to load audio", e)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to load audio. Press HOME to refresh the page."
            Log.e(tag, "Failed to load audio", e)
        } finally {
            _isAudioLoading.remove(audioId)
        }
    }

    private fun playAudio(audioId: String) {

        val audioData = _audioResources.getNullable(audioId)
        if (audioData == null) {
            Log.d(tag, "playAudion: no audio data for [audioId = $audioId]")
            return
        }

        if (setAudioIsPlaying(audioId)) {
            Log.d(tag, "playAudion: audio is already playing for [audioId = $audioId]")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            var tempFile: Path? = null
            try {
                val tmpFileName = audioId
                    .replace(":", "-")
                    .replace(" ", "")
                    .replace(",", "-")
                tempFile =
                    Files.createTempFile("temp-audio-$tmpFileName-", ".mp3")
                Log.d(
                    tag,
                    "playAudion: create temp file $tempFile for [audioId = $audioId]"
                )
                tempFile.outputStream().use {
                    it.write(audioData)
                }

                withContext(Dispatchers.Main) {
                    val exoPlayer = ExoPlayer.Builder(context).build()

                    exoPlayer.apply {
                        val mediaItem = MediaItem.fromUri(tempFile.toUri().toString())
                        setMediaItem(mediaItem)
                        prepare()
                        playWhenReady = true

                        addListener(object : Player.Listener {
                            override fun onPlaybackStateChanged(playbackState: Int) {
                                if (playbackState == Player.STATE_ENDED) {
                                    _isAudioPlaying[audioId] = false
                                    Log.d(
                                        tag,
                                        "playAudio: playback completed [audioId = $audioId]"
                                    )
                                    this@apply.onFinish(audioId, tempFile)
                                }
                            }

                            override fun onPlayerError(error: PlaybackException) {
                                Log.e(
                                    tag,
                                    "playAudio: error occurred [audioId = $audioId]: ${error.message}",
                                    error
                                )
                                this@apply.onFinish(audioId, tempFile)
                            }
                        })

                        activeMediaPlayers[audioId] = this
                        Log.d(tag, "playAudio: start playing [audioId = $audioId]")
                        play()
                    }
                }
            } catch (e: Exception) {
                Log.e(
                    tag,
                    "playAudion: failed [audioId = $audioId]: ${e.localizedMessage}",
                    e
                )
                releaseAudioResources(audioId, tempFile)
            }
        }
    }

    private fun ExoPlayer.onFinish(audioId: String, tempFile: Path?) {
        try {
            release()
        } catch (_: Exception) {
        }
        releaseAudioResources(audioId, tempFile)
    }

    private fun releaseAudioResources(audioId: String, tempFile: Path?) {
        viewModelScope.launch(Dispatchers.Main) {
            activeMediaPlayers.remove(audioId)
            _isAudioPlaying.remove(audioId)
        }
        viewModelScope.launch(Dispatchers.IO) {
            tempFile?.deleteIfExists()
        }
    }

    suspend fun waitForAudioProcessing(audioId: String) {
        withTimeout(AUDIO_PROCESSING_MAX_DELAY_MS) {
            while (isAudioProcessing(audioId)) {
                delay(100)
            }
        }
    }

    fun isAudioProcessing(audioId: String): Boolean {
        return isAudioLoading(audioId) || isAudioPlaying(audioId)
    }

    private fun isAudioPlaying(audioId: String): Boolean {
        return _isAudioPlaying[audioId] ?: false
    }

    private fun setAudioIsPlaying(audioId: String): Boolean {
        return _isAudioPlaying.putIfAbsent(audioId, true) == true
    }

    private fun isAudioLoaded(audioId: String): Boolean {
        return _audioResources.hasKey(audioId)
    }

    fun isAudioLoading(audioId: String): Boolean {
        return _isAudioLoading[audioId] ?: false
    }

}