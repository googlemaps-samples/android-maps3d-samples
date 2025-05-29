package com.example.advancedmaps3dsamples.ainavigator

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.advancedmaps3dsamples.ainavigator.data.NavigatorService
import com.example.advancedmaps3dsamples.common.Map3dViewModel
import com.example.advancedmaps3dsamples.scenarios.AnimationStep
import com.example.advancedmaps3dsamples.scenarios.ScenarioBaseViewModel
import com.example.advancedmaps3dsamples.scenarios.toAnimation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
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

    private val _isRequestInflight = MutableStateFlow(false)
    val isRequestInflight: StateFlow<Boolean> = _isRequestInflight

    private val _userMessage = Channel<String>()
    val userMessage = _userMessage.receiveAsFlow()

    fun processUserRequest(userInput: String) {
        viewModelScope.launch {
            _isRequestInflight.value = true
            try {
                val animationString = navigatorService.getAnimationString(userInput)
                Log.w(TAG, "Got animationString: $animationString")

                val animation = animationString.toAnimation()

                playAnimation(animation)
            } catch (e: Exception) {
                Log.e(TAG, "Error processing user request", e)
                _userMessage.send("Error processing request: ${e.localizedMessage}")
            } finally {
                _isRequestInflight.value = false
            }
        }
    }

    private fun playAnimation(animation: List<AnimationStep>) {
        animationJob?.cancel()

        animationJob = viewModelScope.launch {
            animation.forEach { step -> step(this@AiNavigatorViewModel) }
        }
    }

    fun cancelRequest() {
        animationJob?.cancel()
        _isRequestInflight.value = false
    }

    override suspend fun showMessage(message: String) {
        _userMessage.send(message)
    }
}
