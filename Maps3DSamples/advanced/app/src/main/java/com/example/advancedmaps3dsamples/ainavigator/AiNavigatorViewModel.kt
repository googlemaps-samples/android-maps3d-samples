package com.example.advancedmaps3dsamples.ainavigator

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.viewModelScope
import com.example.advancedmaps3dsamples.ainavigator.data.NavigatorService
import com.example.advancedmaps3dsamples.ainavigator.data.examplePrompts
import com.example.advancedmaps3dsamples.common.Map3dViewModel
import com.example.advancedmaps3dsamples.scenarios.AnimationStep
import com.example.advancedmaps3dsamples.scenarios.ScenarioBaseViewModel
import com.example.advancedmaps3dsamples.scenarios.toAnimation
import com.example.advancedmaps3dsamples.utils.toCameraString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume

@HiltViewModel
class AiNavigatorViewModel @Inject constructor(
    private val navigatorService: NavigatorService
) : Map3dViewModel(), ScenarioBaseViewModel {
    override val TAG = this::class.java.simpleName
    private var animationJob: Job? = null
    private var currentAnimation: List<AnimationStep> = emptyList()
    private var isAnimating = false

    private val _isRequestInflight = MutableStateFlow(false)
    val isRequestInflight: StateFlow<Boolean> = _isRequestInflight

    private val _userMessage = Channel<String>()
    val userMessage = _userMessage.receiveAsFlow()

    val allPrompts = examplePrompts.toMutableList()

    fun processUserRequest(userInput: String) {
        viewModelScope.launch {
            _isRequestInflight.value = true
            try {
                val animationString = navigatorService.getAnimationString(userInput)
                Log.w(TAG, "Got animationString: $animationString")

                currentAnimation = animationString.toAnimation()
                playAnimation()
            } catch (e: Exception) {
                Log.e(TAG, "Error processing user request", e)
                _userMessage.send("Error processing request: ${e.localizedMessage}")
            } finally {
                _isRequestInflight.value = false
            }
        }
    }

    fun playAnimation() {
        animationJob?.cancel()
        isAnimating = true

        animationJob = viewModelScope.launch {
            for (step in currentAnimation) {
                step(this@AiNavigatorViewModel)
            }
            isAnimating = false
        }
    }

    fun stopAnimation() {
        if (isAnimating && animationJob?.isActive == true) {
            animationJob?.cancel()
            isAnimating = false
            // Call stopAnimations or similar function if needed when animation is stopped by user
            // For example: stopAnimations()
        }
    }

    fun restartAnimation() {
        stopAnimation()
        if (currentAnimation.isNotEmpty()) {
            playAnimation()
        }
    }

    fun cancelRequest() {
        stopAnimation()
        _isRequestInflight.value = false
    }

    override suspend fun showMessage(message: String) {
        _userMessage.send(message)
    }

    fun generateNewPrompts() {
        viewModelScope.launch {
            _isRequestInflight.value = true
            try {
                val newPrompts = navigatorService.getNewPrompts()
                Log.w(TAG, "Got new prompts: $newPrompts")
                if (newPrompts.isNotEmpty()) {
                    allPrompts.clear()
                    allPrompts.addAll(newPrompts)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating new prompts", e)
                _userMessage.send("Error generating new prompts: ${e.localizedMessage}")
            }
            _isRequestInflight.value = false
        }
    }

    fun getRandomPrompt(): String = allPrompts.random()

    fun whatAmILookingAt(bitmap: ImageBitmap) {
        val cameraString = currentCamera.value.toCameraString()
        Log.w(TAG, "What am I looking at? cameraString: $cameraString")

        viewModelScope.launch {
            _isRequestInflight.value = true
            try {
                val whatAmILookingAt = navigatorService.whatAmILookingAt(cameraString, bitmap)
                Log.w(TAG, "Got whatAmILookingAt: $whatAmILookingAt")
                if (whatAmILookingAt.isNotEmpty()) {
                    _userMessage.send(whatAmILookingAt)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting whatAmILookingAt", e)
                _userMessage.send("Error getting whatAmILookingAt: ${e.localizedMessage}")
            }
            _isRequestInflight.value = false
        }
    }
}

suspend fun Bitmap.saveToDisk(context: Context): Uri {
    val file = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        "screenshot-${System.currentTimeMillis()}.png"
    )

    file.writeBitmap(this, Bitmap.CompressFormat.PNG, 100)

    return scanFilePath(context, file.path) ?: throw Exception("File could not be saved")
}

private fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
    outputStream().use { out ->
        bitmap.compress(format, quality, out)
        out.flush()
    }
}

private suspend fun scanFilePath(context: Context, filePath: String): Uri? {
    return suspendCancellableCoroutine { continuation ->
        MediaScannerConnection.scanFile(
            context,
            arrayOf(filePath),
            arrayOf("image/png")
        ) { _, scannedUri ->
            if (scannedUri == null) {
                continuation.cancel(Exception("File $filePath could not be scanned"))
            } else {
                continuation.resume(scannedUri)
            }
        }
    }
}

private fun shareImage(uri: Uri, context: Context) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/jpeg"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share Image"))
}
