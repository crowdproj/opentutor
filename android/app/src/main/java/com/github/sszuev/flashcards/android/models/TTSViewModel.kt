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
import com.github.sszuev.flashcards.android.entities.CardEntity
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

    fun invalidate(cardId: String) {
        _audioResources.remove(cardId)
    }

    fun loadAndPlayAudio(card: CardEntity) {
        val cardId = checkNotNull(card.cardId)
        if (isAudioPlaying(cardId)) {
            Log.d(
                tag,
                "loadAndPlayAudion: audio is already playing for [cardId = $cardId (${card.word})]"
            )
            return
        }
        if (isAudioLoaded(cardId)) {
            playAudio(card)
        } else {
            loadAudio(cardId, card.audioId) {
                playAudio(card)
            }
        }
    }

    private fun loadAudio(cardId: String, audioResourceId: String, onLoaded: () -> Unit) {
        if (_audioResources.hasKeyAndValue(cardId)) {
            onLoaded()
            return
        }
        viewModelScope.launch {
            _loadAudio(cardId, audioResourceId, onLoaded)
        }
    }

    @Suppress("FunctionName")
    private suspend fun _loadAudio(cardId: String, audioResourceId: String, onLoaded: () -> Unit) {
        Log.d(tag, "load audio for card = $cardId")
        _isAudioLoading[cardId] = true
        _errorMessage.value = null
        val lang = langFromAudioResource(audioResourceId)
        val word = wordFromAudioResource(audioResourceId)
        try {
            val resource = withContext(Dispatchers.IO) {
                ttsRepository.get(lang, word)
            }
            _audioResources.putNullable(cardId, resource)
            if (resource != null) {
                onLoaded()
            }
        } catch (e: InvalidTokenException) {
            signOut()
        } catch (e: ApiResponseException) {
            _audioResources.putNullable(cardId, null)
            Log.e(tag, "Failed to load audio", e)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to load audio: ${e.localizedMessage}"
            Log.e(tag, "Failed to load audio", e)
        } finally {
            _isAudioLoading.remove(cardId)
        }
    }

    private fun playAudio(card: CardEntity) {
        val cardId = checkNotNull(card.cardId)

        val audioData = _audioResources.getNullable(cardId)
        if (audioData == null) {
            Log.d(tag, "playAudion: no audio data for [cardId = $cardId (${card.word})]")
            return
        }

        if (setAudioIsPlaying(cardId)) {
            Log.d(tag, "playAudion: audio is already playing for [cardId = $cardId (${card.word})]")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            var tempFile: Path? = null
            try {
                val tmpFileName = card.audioId
                    .replace(":", "-")
                    .replace(" ", "")
                    .replace(",", "-")
                tempFile =
                    Files.createTempFile("temp-audio-$tmpFileName-", ".mp3")
                Log.d(
                    tag,
                    "playAudion: create temp file $tempFile for [cardId = $cardId (${card.word})]"
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
                                    _isAudioPlaying[cardId] = false
                                    Log.d(
                                        tag,
                                        "playAudio: playback completed [cardId = $cardId (${card.word})]"
                                    )
                                    this@apply.onFinish(cardId, tempFile)
                                }
                            }

                            override fun onPlayerError(error: PlaybackException) {
                                Log.e(
                                    tag,
                                    "playAudio: error occurred [cardId = $cardId (${card.word})]: ${error.message}",
                                    error
                                )
                                this@apply.onFinish(cardId, tempFile)
                            }
                        })

                        activeMediaPlayers[cardId] = this
                        Log.d(tag, "playAudio: start playing [cardId = $cardId (${card.word})]")
                        play()
                    }
                }
            } catch (e: Exception) {
                Log.e(
                    tag,
                    "playAudion: failed [cardId = $cardId (${card.word})]: ${e.localizedMessage}",
                    e
                )
                releaseAudioResources(cardId, tempFile)
            }
        }
    }

    private fun ExoPlayer.onFinish(cardId: String, tempFile: Path?) {
        try {
            release()
        } catch (_: Exception) {
        }
        releaseAudioResources(cardId, tempFile)
    }

    private fun releaseAudioResources(cardId: String, tempFile: Path?) {
        viewModelScope.launch(Dispatchers.Main) {
            activeMediaPlayers.remove(cardId)
            _isAudioPlaying.remove(cardId)
        }
        viewModelScope.launch(Dispatchers.IO) {
            tempFile?.deleteIfExists()
        }
    }

    suspend fun waitForAudioProcessing(cardId: String) {
        withTimeout(AUDIO_PROCESSING_MAX_DELAY_MS) {
            while (isAudioProcessing(cardId)) {
                delay(100)
            }
        }
    }

    fun isAudioProcessing(cardId: String): Boolean {
        return isAudioLoading(cardId) || isAudioPlaying(cardId)
    }

    private fun isAudioPlaying(cardId: String): Boolean {
        return _isAudioPlaying[cardId] ?: false
    }

    private fun setAudioIsPlaying(cardId: String): Boolean {
        return _isAudioPlaying.putIfAbsent(cardId, true) == true
    }

    private fun isAudioLoaded(cardId: String): Boolean {
        return _audioResources.hasKey(cardId)
    }

    fun isAudioLoading(cardId: String): Boolean {
        return _isAudioLoading[cardId] ?: false
    }

}