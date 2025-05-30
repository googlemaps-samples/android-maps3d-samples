package com.example.advancedmaps3dsamples.ainavigator.data

import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.graphics.scale

@Singleton
class NavigatorService @Inject constructor(
) {
    val TAG = this::class.java.simpleName

    val model by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-2.5-flash-preview-05-20")
    }

    suspend fun getAnimationString(userInput: String, cameraString: String): String {
        Log.d(TAG, "Calling Firebase Vertex AI: Fetching animationString for user input: $userInput")

        try {
            // --- Use Firebase SDK's generateContent ---
            val response = model.generateContent(promptWithCamera + "\n" + userInput + "\n" + cameraString)
            // -----------------------------------------
            Log.d(TAG, "Firebase Vertex AI raw response: ${response.text}")
            // Clean potential markdown code blocks from riddle response as well
            val cleanedText = response.text?.sanitize()?.removePrefix("animationString=")?.removeSurrounding("\"")?.replace("\\\"", "\"")
            Log.d(TAG, "Firebase Vertex AI cleaned response: $cleanedText")
            return cleanedText ?: ""  // TODO: default animation?  Do a barrel roll...?
        } catch (e: Exception) {
            // TODO: Handle specific Firebase/Vertex AI exceptions if needed
            Log.e(TAG, "Error getting animation from Firebase Vertex AI for $userInput", e)
            throw GameRepositoryException("Unable to get animation: ${e.message}", e)
        }
    }

    suspend fun getNewPrompts(): List<String> {
        Log.d(TAG, "Calling Firebase Vertex AI: Fetching new prompts")
        try {
            // --- Use Firebase SDK's generateContent ---
            val response = model.generateContent(promptGeneratorPrompt)
            // -----------------------------------------
            Log.d(TAG, "Firebase Vertex AI raw response: ${response.text}")
            // Clean potential markdown code blocks from riddle response as well
            val cleanedText = response.text?.sanitize()
            Log.d(TAG, "Firebase Vertex AI cleaned response: $cleanedText")
            return cleanedText?.split("\n") ?: emptyList()
        } catch (e: Exception) {
            // TODO: Handle specific Firebase/Vertex AI exceptions if needed
            Log.e(TAG, "Error getting prompts from Firebase Vertex AI", e)
            throw GameRepositoryException("Unable to get prompts: ${e.message}", e)
        }
    }

    suspend fun whatAmILookingAt(cameraParams: String, bitmap: ImageBitmap): String {
        Log.d(TAG, "Calling Firebase Vertex AI: Fetching whatAmILookingAt for cameraParams: $cameraParams")
        try {
            // Convert the Jetpack Compose ImageBitmap to an Android Bitmap
            val originalBitmap = bitmap.asAndroidBitmap()

            // --- Start: Image Scaling Logic ---
            // Calculate the new dimensions (50% of the original)
            val newWidth = originalBitmap.width / 2
            val newHeight = originalBitmap.height / 2

            // Create a new bitmap scaled to the new dimensions.
            // The 'true' flag enables filtering for better quality.
            val scaledBitmap = originalBitmap.scale(newWidth, newHeight)
            // --- End: Image Scaling Logic ---

            val inputContent = content {
                // Use the newly created scaledBitmap in the request
                image(scaledBitmap)
                text(whatAmILookingAtPrompt.replace("<cameraParams>", cameraParams) + "")
            }

            val response = model.generateContent(inputContent)
            Log.d(TAG, "Firebase Vertex AI raw response: ${response.text}")
            val cleanedText = response.text?.sanitize()
            Log.d(TAG, "Firebase Vertex AI cleaned response: $cleanedText")
            return cleanedText ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Error getting whatAmILookingAt from Firebase Vertex AI for $cameraParams", e)
            throw GameRepositoryException("Unable to get whatAmILookingAt: ${e.message}", e)
        }
    }
}

// Define a custom exception class for clarity (Optional)
class GameRepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)

private fun String.sanitize(): String {
    return trim()
        .removeSurrounding("```json", "```").trim()
        .removeSurrounding("```javascript", "```").trim()
        .removeSurrounding("```python", "```").trim()
        .removeSurrounding("```", "```").trim()
        .removeSurrounding("`")
}