/*
 * Copyright 2025 Google LLC
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

package com.example.maps3djava.mainactivity;

import android.content.Intent;
import android.os.Bundle;
import com.example.maps3dcommon.R;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.maps3djava.cameracontrols.CameraControlsActivity;
import com.example.maps3djava.hellomap.HelloMapActivity;
import com.example.maps3djava.markers.MarkersActivity;
import com.example.maps3djava.models.ModelsActivity;
import com.example.maps3djava.polygons.PolygonsActivity;
import com.example.maps3djava.polylines.PolylinesActivity;

import java.util.LinkedHashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private final Map<Integer, Class<?>> sampleActivities = new LinkedHashMap<>() {{
        put(R.string.feature_title_overview_hello_3d_map, HelloMapActivity.class);
        put(R.string.feature_title_camera_controls, CameraControlsActivity.class);
        put(R.string.feature_title_markers, MarkersActivity.class);
        put(R.string.feature_title_3d_models, ModelsActivity.class);
        put(R.string.feature_title_polygons, PolygonsActivity.class);
        put(R.string.feature_title_polylines, PolylinesActivity.class);
        put(R.string.feature_title_3d_models, ModelsActivity.class);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.maps3djava.R.layout.main_activity);

        RecyclerView recyclerView = findViewById(com.example.maps3djava.R.id.sample_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new SampleListAdapter(sampleActivities, this::onSampleClicked));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    private void onSampleClicked(Class<?> activityClass, @StringRes int titleResId) {
        if (activityClass != null) {
            Intent intent = new Intent(this, activityClass);
            startActivity(intent);
        }
    }
}
