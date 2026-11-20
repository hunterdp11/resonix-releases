package com.callscreener.app.engine

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.Locale

class CallTranscriptionManager(private val context: Context) : RecognitionListener {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    private val _transcriptionFlow = MutableSharedFlow<TranscriptionResult>(extraBufferCapacity = 64)
    val transcriptionFlow: SharedFlow<TranscriptionResult> = _transcriptionFlow.asSharedFlow()

    data class TranscriptionResult(
        val text: String,
        val isFinal: Boolean
    )

    fun startListening() {
        mainHandler.post {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                Log.w(TAG, "Speech recognition is not available on this device")
                return@post
            }

            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(this@CallTranscriptionManager)
                }
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }

            try {
                speechRecognizer?.startListening(intent)
                isListening = true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start speech recognizer", e)
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            if (isListening) {
                try {
                    speechRecognizer?.cancel()
                    speechRecognizer?.stopListening()
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping recognizer", e)
                }
                isListening = false
            }
        }
    }

    fun release() {
        mainHandler.post {
            stopListening()
            try {
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                Log.e(TAG, "Error destroying speech recognizer", e)
            }
            speechRecognizer = null
        }
    }

    override fun onReadyForSpeech(params: Bundle?) {
        Log.d(TAG, "Ready for incoming caller speech")
    }

    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {
        isListening = false
    }

    override fun onError(error: Int) {
        isListening = false
        Log.w(TAG, "SpeechRecognizer error: $error")
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val bestMatch = matches[0]
            _transcriptionFlow.tryEmit(TranscriptionResult(text = bestMatch, isFinal = true))
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val partialText = matches[0]
            _transcriptionFlow.tryEmit(TranscriptionResult(text = partialText, isFinal = false))
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    companion object {
        private const val TAG = "CallTranscription"
    }
}
