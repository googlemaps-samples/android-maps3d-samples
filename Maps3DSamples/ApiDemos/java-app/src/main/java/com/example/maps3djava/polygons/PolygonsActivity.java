package com.example.maps3djava.polygons;

// Copyright 2025 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


import static com.example.maps3d.common.UtilitiesKt.toValidCamera;
import static com.example.maps3djava.cameracontrols.DataModel.extrudePolygon;

import android.graphics.Color;
import android.widget.Toast;


import com.example.maps3d.common.UnitsKt;
import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.OnPolygonClickListener;
import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.Hole;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Map3DMode;
import com.google.android.gms.maps3d.model.PolygonOptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PolygonsActivity extends SampleBaseActivity {

    private static final Hole zooHole = new Hole(Arrays.stream(
                    ("39.7498, -104.9535\n" +
                            "39.7498, -104.9525\n" +
                            "39.7488, -104.9525\n" +
                            "39.7488, -104.9535\n" +
                            "39.7498, -104.9535")
                            .trim().split("\\R"))
            .map(line -> Arrays.stream(line.split(","))
                    .map(String::trim)
                    .map(Double::parseDouble)
                    .collect(Collectors.toList()))
            .map(coords -> new LatLngAltitude(coords.get(0), coords.get(1), 0.0))
            .collect(Collectors.toUnmodifiableList()));

    private static final List<LatLngAltitude> zooOutline = Arrays.stream(
                    ("39.7508987, -104.9565381\n" +
                            "39.7502883, -104.9565489\n" +
                            "39.7501976, -104.9563557\n" +
                            "39.7501481, -104.955594\n" +
                            "39.7499171, -104.9553043\n" +
                            "39.7495872, -104.9551648\n" +
                            "39.7492407, -104.954961\n" +
                            "39.7489685, -104.9548859\n" +
                            "39.7484488, -104.9548966\n" +
                            "39.7481189, -104.9548859\n" +
                            "39.7479539, -104.9547679\n" +
                            "39.7479209, -104.9544567\n" +
                            "39.7476487, -104.9535341\n" +
                            "39.7475085, -104.9525792\n" +
                            "39.7474095, -104.9519247\n" +
                            "39.747525, -104.9513776\n" +
                            "39.7476734, -104.9511844\n" +
                            "39.7478137, -104.9506265\n" +
                            "39.7477559, -104.9496395\n" +
                            "39.7477477, -104.9486203\n" +
                            "39.7478467, -104.9475796\n" +
                            "39.7482344, -104.9465818\n" +
                            "39.7486138, -104.9457878\n" +
                            "39.7491005, -104.9454874\n" +
                            "39.7495789, -104.945938\n" +
                            "39.7500491, -104.9466998\n" +
                            "39.7503213, -104.9474615\n" +
                            "39.7505358, -104.9486954\n" +
                            "39.7505111, -104.950648\n" +
                            "39.7511215, -104.9506587\n" +
                            "39.7511173, -104.9527187\n" +
                            "39.7511091, -104.9546445\n" +
                            "39.7508987, -104.9565381").trim().split("\\R"))
            .map(line -> Arrays.stream(line.split(","))
                    .map(String::trim)
                    .map(Double::parseDouble)
                    .collect(Collectors.toList()))
            .map(coords -> new LatLngAltitude(coords.get(0), coords.get(1), 0.0))
            .collect(Collectors.toUnmodifiableList());

    @Override
    public final String getTAG() {
        return this.getClass().getSimpleName();
    }

    private final String TAG = getTAG();

    @Override
    public final Camera getInitialCamera() {
        return toValidCamera(new Camera(
                new LatLngAltitude(
                        DENVER_LATITUDE,
                        DENVER_LONGITUDE,
                        UnitsKt.getMeters(1.0)

                ),
                -68.0,
                47.0,
                0.0,
                2251.0
        ));
    }

    private final int faceFillColor = Color.argb(70, 255, 0, 255);
    private final int faceStrokeColor = Color.MAGENTA;
    private final double faceStrokeWidth = 3.0;

    private final List<PolygonOptions> extrudedMuseum = extrudePolygon(
            museumBaseFace, 50.0).stream().map(outline -> {
        PolygonOptions options = new PolygonOptions();
        options.setPath(outline);
        options.setFillColor(faceFillColor);
        options.setStrokeColor(faceStrokeColor);
        options.setStrokeWidth(faceStrokeWidth);
        options.setAltitudeMode(AltitudeMode.CLAMP_TO_GROUND);
        options.setGeodesic(false);
        options.setDrawsOccludedSegments(true);
        return options;
    }).collect(Collectors.toList());

    @Override
    public void onMap3DViewReady(GoogleMap3D googleMap3D) {
        super.onMap3DViewReady(googleMap3D);
        googleMap3D.setMapMode(Map3DMode.HYBRID);

        // Add extruded polygons to the map. The returned list of polygons can be used to remove them at a later time.
        // The addPolygon method returns a Polygon object, not PolygonOptions
        List<com.google.android.gms.maps3d.model.Polygon> museumPolygons = extrudedMuseum.stream()
                .map(googleMap3D::addPolygon)
                .collect(Collectors.toList());

        for (com.google.android.gms.maps3d.model.Polygon polygon : museumPolygons) {
            polygon.setClickListener(() -> PolygonsActivity.this.runOnUiThread(() -> {
                Toast.makeText(PolygonsActivity.this, "Check out the Museum!", Toast.LENGTH_SHORT).show();
            }));
        }

        // Add a zoo polygon to the map.
        com.google.android.gms.maps3d.model.Polygon zooPolygon = googleMap3D.addPolygon(zooPolygonOptions);
        zooPolygon.setClickListener(() -> PolygonsActivity.this.runOnUiThread(() -> {
            Toast.makeText(PolygonsActivity.this, "Zoo time", Toast.LENGTH_SHORT).show();
        }));
    }

    public static class Companion {

        private static final double DENVER_LATITUDE = 39.748477;
        private static final double DENVER_LONGITUDE = -104.947575;

        private static final double museumAltitude = UnitsKt.getMeters(1.0);

        private static final List<LatLngAltitude> museumBaseFace = Arrays.stream(
                        ("39.74812392425406, -104.94414971628434\n" +
                                "39.7465307929639, -104.94370889409778\n" +
                                "39.747031745033794, -104.9415078562927\n" +
                                "39.74837320615968, -104.94194414397013\n" +
                                "39.74812392425406, -104.94414971628434")
                                .trim().split("\\R")) // Use \\R for any Unicode newline sequence
                .map(line -> Arrays.stream(line.split(","))
                        .map(String::trim)
                        .map(Double::parseDouble)
                        .collect(Collectors.toList()))
                .map(coords -> new LatLngAltitude(coords.get(0), coords.get(1), museumAltitude))
                .collect(Collectors.toUnmodifiableList());

    }


    private static final double DENVER_LATITUDE = Companion.DENVER_LATITUDE;
    private static final double DENVER_LONGITUDE = Companion.DENVER_LONGITUDE;

    public static final PolygonOptions zooPolygonOptions;

    static {
        zooPolygonOptions = new PolygonOptions();
        zooPolygonOptions.setPath(PolygonsActivity.zooOutline);
        zooPolygonOptions.setInnerPaths(Collections.singletonList(PolygonsActivity.zooHole));
        zooPolygonOptions.setFillColor(Color.argb(70, 255, 255, 0));
        zooPolygonOptions.setStrokeColor(Color.GREEN);
        zooPolygonOptions.setStrokeWidth(3.0);
        zooPolygonOptions.setAltitudeMode(AltitudeMode.CLAMP_TO_GROUND);
    }
    private static final List<LatLngAltitude> museumBaseFace = Companion.museumBaseFace;
}