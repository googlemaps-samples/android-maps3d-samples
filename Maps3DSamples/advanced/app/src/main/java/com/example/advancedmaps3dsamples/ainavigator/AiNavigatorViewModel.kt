package com.example.advancedmaps3dsamples.ainavigator

import android.util.Log
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
import javax.inject.Inject

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

    fun whatAmILookingAt() {
        val cameraString = currentCamera.value.toCameraString()
        Log.w(TAG, "What am I looking at? cameraString: $cameraString")

        viewModelScope.launch {
            _isRequestInflight.value = true
            try {
                val whatAmILookingAt = navigatorService.whatAmILookingAt(cameraString)
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
