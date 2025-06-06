/*
 * Copyright 2023 Google LLC
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

package com.google.android.apidemos.java.hellomap;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.Map3DView;
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback;
// Assuming the common module's R file will be correctly referenced.
// If not, this might need adjustment during compilation (e.g. if common's package is different)
import com.example.maps3dcommon.R;


public class HelloMapActivity extends Activity implements OnMap3DViewReadyCallback {
    private static final String TAG = HelloMapActivity.class.getSimpleName();
    private Map3DView map3DView;
    private GoogleMap3D googleMap3D;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Uses layout from the 'common' module
        setContentView(R.layout.activity_hello_map);

        // Uses ID from the 'common' module's layout
        View rootView = findViewById(R.id.map_container);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            WindowInsetsCompat statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            WindowInsetsCompat navigationBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());

            // Apply insets as padding
            v.setPadding(
                v.getPaddingLeft(), // Keep original left padding
                v.getPaddingTop() + statusBarInsets.top,
                v.getPaddingRight(), // Keep original right padding
                v.getPaddingBottom() + navigationBarInsets.bottom
            );
            return WindowInsetsCompat.CONSUMED;
        });

        // Uses ID from the 'common' module's layout
        map3DView = findViewById(R.id.map3dView);
        map3DView.onCreate(savedInstanceState);
        map3DView.getMap3DViewAsync(this);
    }

    @Override
    public void onMap3DViewReady(GoogleMap3D map) {
        this.googleMap3D = map;
        // Map is ready for interaction.
        // For example, you could set camera position here:
        // googleMap3D.setCamera(new Camera.Builder().target(new LatLng(latitude, longitude)).zoom(15f).build());
    }

    @Override
    public void onError(Exception exception) {
        Log.e(TAG, "Error loading map", exception);
        // The OnMap3DViewReadyCallback interface defines onError(Exception),
        // Activity class itself does not have a direct super.onError(Exception) to call.
        // If a base class like AppCompatActivity had an onError, we'd call it.
        // For now, just logging is appropriate based on the interface.
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (map3DView != null) {
            map3DView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (map3DView != null) {
            map3DView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (map3DView != null) {
            map3DView.onDestroy();
        }
    }

    @SuppressWarnings("deprecation") // For onLowMemory
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (map3DView != null) {
            map3DView.onLowMemory();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (map3DView != null) {
            map3DView.onSaveInstanceState(outState);
        }
    }
}
