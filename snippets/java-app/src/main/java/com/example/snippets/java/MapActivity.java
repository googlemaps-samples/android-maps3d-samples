/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
  private String groupTitle;
  private GoogleMap3D map;
  private int currentIndex = -1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_map);
    map3DView = findViewById(R.id.map);
    map3DView.onCreate(savedInstanceState);

    findViewById(R.id.snapshot_button).setOnClickListener(v -> {
        if (map != null) {
            com.google.android.gms.maps3d.model.Camera cam = map.getCamera();
            if (cam != null && cam.getCenter() != null) {
                com.google.android.gms.maps3d.model.LatLngAltitude center = cam.getCenter();
                double rawHeading = cam.getHeading() != null ? cam.getHeading() : 0.0;
                double heading = (rawHeading % 360.0 + 360.0) % 360.0;

                double rawRoll = cam.getRoll() != null ? cam.getRoll() : 0.0;
                double roll = rawRoll == -0.0 ? 0.0 : rawRoll;

                Log.d("MapActivity", String.format(
                    "Camera Pose:\ncenter = latLngAltitude {\n    latitude = %.6f\n    longitude = %.6f\n    altitude = %.2f\n}\ntilt = %.2f\nheading = %.2f\nrange = %.2f\nroll = %.2f",
                    center.getLatitude(), center.getLongitude(), center.getAltitude(),
                    cam.getTilt() != null ? cam.getTilt() : 0.0, heading, cam.getRange() != null ? cam.getRange() : 0.0, roll
                ));
            }
        } else {
            Log.d("MapActivity", "Map NOT initialized yet");
        }
    });

    findViewById(R.id.reset_view_button).setOnClickListener(v -> runSnippet());

    groupTitle = getIntent().getStringExtra("group_title");
    snippetTitle = getIntent().getStringExtra(EXTRA_SNIPPET_TITLE);
    final java.util.List<SnippetItemInfo> snippetList = new java.util.ArrayList<>();
    for (SnippetGroupInfo group : SnippetRegistry.getSnippetGroups()) {
        snippetList.addAll(group.getItems());
    }
    for (int i = 0; i < snippetList.size(); i++) {
        SnippetItemInfo item = snippetList.get(i);
        if (item.getTitle().equals(snippetTitle) && (groupTitle == null || item.getGroupTitle().equals(groupTitle))) {
            currentIndex = i;
            break;
        }
    }

    findViewById(R.id.previous_button).setOnClickListener(v -> {
        if (snippetList.isEmpty()) return;
        if (currentIndex > 0) {
            currentIndex--;
        } else {
            currentIndex = snippetList.size() - 1;
        }
        SnippetItemInfo item = snippetList.get(currentIndex);
        groupTitle = item.getGroupTitle();
        snippetTitle = item.getTitle();
        runSnippet();
    });

    findViewById(R.id.next_button).setOnClickListener(v -> {
        if (snippetList.isEmpty()) return;
        if (currentIndex < snippetList.size() - 1) {
            currentIndex++;
        } else {
            currentIndex = 0;
        }
        SnippetItemInfo item = snippetList.get(currentIndex);
        groupTitle = item.getGroupTitle();
        snippetTitle = item.getTitle();
        runSnippet();
    });

    map3DView.getMap3DViewAsync(new OnMap3DViewReadyCallback() {
      @Override
      public void onMap3DViewReady(@NonNull GoogleMap3D map) {
        MapActivity.this.map = map;
        Log.w("MapActivity", "onMap3DViewReady " + map);

        // Simple 3-second delay to bypass readiness bugs as requested
        runSnippet(); 
      }

      @Override
      public void onError(@NonNull Exception error) {
        runOnUiThread(() -> Toast.makeText(MapActivity.this, "Map Error: " + error.getMessage(), Toast.LENGTH_LONG).show());
      }
    });
  }

  protected void runSnippet() {
    if (snippetTitle != null) {
      String key = groupTitle != null ? groupTitle + " - " + snippetTitle : snippetTitle;
      SnippetItemInfo snippet = SnippetRegistry.snippets.get(key);
      if (snippet == null) {
           // Fallback linear scan
           for (SnippetGroupInfo g : SnippetRegistry.getSnippetGroups()) {
               for (SnippetItemInfo item : g.getItems()) {
                    if (item.getTitle().equals(snippetTitle)) {
                        snippet = item;
                        break;
                    }
               }
           }
       }
       final SnippetItemInfo finalSnippet = snippet;
       if (finalSnippet != null) {
         SnippetRegistry.clearTrackedItems();
         // 3 second delay
         new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
             try {
               finalSnippet.getAction().execute(MapActivity.this, map);
               runOnUiThread(() -> Toast.makeText(
                   MapActivity.this,
                   finalSnippet.getGroupTitle() + ": " + finalSnippet.getTitle() + ".\n" + finalSnippet.getDescription(),
                   Toast.LENGTH_LONG
               ).show());
             } catch (Exception e) {
               runOnUiThread(() -> Toast.makeText(MapActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
               e.printStackTrace();
             }
         }, 3000);
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
