package com.example.snippets.java;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;

import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps3d.GoogleMap3D;

import com.google.android.gms.maps3d.OnMap3DViewReadyCallback;

import com.example.snippets.common.R;

public class MapActivity extends AppCompatActivity {

  public static final String EXTRA_SNIPPET_TITLE = "snippet_title";

  private com.google.android.gms.maps3d.Map3DView map3DView;

  private boolean triggered = false;

  private String snippetTitle;
  private GoogleMap3D map;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_map);
    map3DView = findViewById(R.id.map);
    map3DView.onCreate(savedInstanceState);

    snippetTitle = getIntent().getStringExtra(EXTRA_SNIPPET_TITLE);
    map3DView.getMap3DViewAsync(new OnMap3DViewReadyCallback() {
      @Override
      public void onMap3DViewReady(@NonNull GoogleMap3D map) {
        MapActivity.this.map = map;
        Log.w("MapActivity", "onMap3DViewReady" + map);

        runSnippet();  // <-- This will fail if the map is not ready

        map.setOnMapReadyListener(sceneReadiness -> {
          if (!triggered && sceneReadiness >= 99.0) {
            Log.w("MapActivity", "onMapReady sceneReadiness: " + sceneReadiness);
            triggered = true;
            runSnippet();  // <-- This will only be called the first time the map is ready
          }
        });
      }

      @Override
      public void onError(@NonNull Exception error) {
        runOnUiThread(() -> Toast.makeText(MapActivity.this, "Map Error: " + error.getMessage(), Toast.LENGTH_LONG).show());
      }
    });
  }

  protected void runSnippet() {
    // Map is ready
    if (snippetTitle != null) {
      Snippet snippet = SnippetRegistry.snippets.get(snippetTitle);
      if (snippet != null) {
        try {
          snippet.action.execute(MapActivity.this, map);
          runOnUiThread(() -> Toast.makeText(MapActivity.this, "Running: " + snippetTitle, Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
          runOnUiThread(() -> Toast.makeText(MapActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
          e.printStackTrace();
        }
      } else {
        runOnUiThread(() -> Toast.makeText(MapActivity.this, "Snippet not found: " + snippetTitle, Toast.LENGTH_LONG).show());
      }
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (map3DView != null)
      map3DView.onStart();
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (map3DView != null)
      map3DView.onResume();
  }

  @Override
  protected void onPause() {
    if (map3DView != null)
      map3DView.onPause();
    super.onPause();
  }

  @Override
  protected void onStop() {
    if (map3DView != null)
      map3DView.onStop();
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    if (map3DView != null)
      map3DView.onDestroy();
    super.onDestroy();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    if (map3DView != null)
      map3DView.onLowMemory();
  }

  @Override
  protected void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    if (map3DView != null)
      map3DView.onSaveInstanceState(outState);
  }
}
