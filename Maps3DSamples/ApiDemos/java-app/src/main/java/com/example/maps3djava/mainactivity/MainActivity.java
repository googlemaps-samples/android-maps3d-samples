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
