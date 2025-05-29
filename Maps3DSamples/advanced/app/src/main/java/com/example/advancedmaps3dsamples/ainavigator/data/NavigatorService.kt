package com.example.advancedmaps3dsamples.ainavigator.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NavigatorService @Inject constructor(
) {
    val TAG = this::class.java.simpleName

    val model by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-2.5-flash-preview-05-20")
    }

    suspend fun getAnimationString(userInput: String): String {
        Log.d(TAG, "Calling Firebase Vertex AI: Fetching animationString for user input: $userInput")

        try {
            // --- Use Firebase SDK's generateContent ---
            val response = model.generateContent(prompt + userInput)
            // -----------------------------------------
            Log.d(TAG, "Firebase Vertex AI raw response: ${response.text}")
            // Clean potential markdown code blocks from riddle response as well
            val cleanedText = response.text?.sanitize()?.removePrefix("animationString=")?.removeSurrounding("\"")
            Log.d(TAG, "Firebase Vertex AI cleaned response: $cleanedText")
            return cleanedText ?: ""  // TODO: default animation?  Do a barrel roll...?
        } catch (e: Exception) {
            // TODO: Handle specific Firebase/Vertex AI exceptions if needed
            Log.e(TAG, "Error getting animation from Firebase Vertex AI for $userInput", e)
            throw GameRepositoryException("Unable to get animation: ${e.message}", e)
        }
    }
}

// Define a custom exception class for clarity (Optional)
class GameRepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)

private fun String.sanitize(): String {
    return trim()
        .removeSurrounding("```json", "```").trim()
        .removeSurrounding("```", "```").trim()
        .removeSurrounding("`")
}