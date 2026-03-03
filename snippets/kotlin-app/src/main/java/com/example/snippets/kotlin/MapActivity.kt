package com.example.snippets.kotlin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.snippets.common.R
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback

class MapActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SNIPPET_TITLE = "snippet_title"
    }

    private lateinit var map3DView: Map3DView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_map)

        map3DView = findViewById(R.id.map)
        map3DView.onCreate(savedInstanceState)

        val snippetTitle = intent.getStringExtra(EXTRA_SNIPPET_TITLE)
        
        map3DView.getMap3DViewAsync(object : OnMap3DViewReadyCallback {
            override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
                // For simplicity, we run immediately, but add a listener for logs
                googleMap3D.setOnMapReadyListener { sceneReadiness ->
                     if (sceneReadiness == 1.0) {
                         // Toast.makeText(this@MapActivity, "Map Ready", Toast.LENGTH_SHORT).show()
                     }
                }
                
                if (snippetTitle != null) {
                    val snippet = SnippetRegistry.snippets[snippetTitle]
                    if (snippet != null) {
                        try {
                            snippet.action(this@MapActivity, googleMap3D)
                            Toast.makeText(this@MapActivity, "Running: $snippetTitle", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(this@MapActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            e.printStackTrace()
                        }
                    } else {
                         Toast.makeText(this@MapActivity, "Snippet not found: $snippetTitle", Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onError(error: java.lang.Exception) {
                Toast.makeText(this@MapActivity, "Map Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onStart() {
        super.onStart()
        map3DView.onStart()
    }

    override fun onResume() {
        super.onResume()
        map3DView.onResume()
    }

    override fun onPause() {
        map3DView.onPause()
        super.onPause()
    }

    override fun onStop() {
        map3DView.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        map3DView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        map3DView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        map3DView.onSaveInstanceState(outState)
    }
}
