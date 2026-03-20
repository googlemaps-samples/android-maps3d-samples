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

package com.example.maps3djava.popovers;

import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.Popover;
import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.CollisionBehavior;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.gms.maps3d.model.Map3DMode;
import com.google.android.gms.maps3d.model.Marker;
import com.google.android.gms.maps3d.model.MarkerOptions;
import com.google.android.gms.maps3d.model.PopoverOptions;
import com.google.android.gms.maps3d.model.PopoverShadow;
import com.google.android.gms.maps3d.model.PopoverStyle;

public class PopoversActivity extends SampleBaseActivity {

  private static final double CONTENT_LAT = 37.820642;
  private static final double CONTENT_LNG = -122.478227;
  private static final double CONTENT_ALT = 0.0;

  private Popover popover = null;
  private int popoverToggleCount = 0;

  @NonNull
  @Override
  public String getTAG() {
    return "PopoversActivity";
  }

  @NonNull
  @Override
  public Camera getInitialCamera() {
    return com.example.maps3d.common.UtilitiesKt.toValidCamera(new Camera(
        new LatLngAltitude(CONTENT_LAT, CONTENT_LNG, CONTENT_ALT),
        0.0,
        45.0,
        0.0,
        4075.0));
  }

  @Override
  public void onMap3DViewReady(GoogleMap3D googleMap3D) {
    super.onMap3DViewReady(googleMap3D);
    googleMap3D.setOnMapReadyListener((map) -> {
      googleMap3D.setOnMapReadyListener(null);
      onMapReady(googleMap3D);
    });
  }

  private void onMapReady(@NonNull GoogleMap3D googleMap3D) {
    googleMap3D.setMapMode(Map3DMode.SATELLITE);
    setupPopover(googleMap3D);
  }

  public void setupPopover(GoogleMap3D googleMap3D) {
    LatLngAltitude center = new LatLngAltitude(37.819852, -122.478549, 0.0);

    MarkerOptions markerOptions = new MarkerOptions();
    markerOptions.setPosition(center);
    markerOptions.setLabel("Golden Gate Bridge");
    markerOptions.setZIndex(1);
    markerOptions.setExtruded(true);
    markerOptions.setDrawnWhenOccluded(true);
    markerOptions.setCollisionBehavior(CollisionBehavior.REQUIRED);
    markerOptions.setAltitudeMode(AltitudeMode.RELATIVE_TO_MESH);

    Marker markerInGoldenGate = googleMap3D.addMarker(markerOptions);

    if (markerInGoldenGate == null) {
      Log.e(getTAG(), "Failed to create marker");
      return;
    } else {
      Log.w(getTAG(), "Marker created");
    }

    PopoverShadow shadow = new PopoverShadow();
    shadow.setColor(Color.argb(77, 0, 0, 0)); // 0.3 * 255 = 76.5 ~= 77
    shadow.setOffsetX(2.0f);
    shadow.setOffsetY(4.0f);
    shadow.setRadius(4.0f);

    PopoverStyle style = new PopoverStyle();
    style.setPadding(20.0f);
    style.setBackgroundColor(Color.WHITE);
    style.setBorderRadius(8.0f);
    style.setShadow(shadow);

    PopoverOptions popoverOptions = new PopoverOptions();
    popoverOptions.setContent(createGoldenGateInfoView());
    popoverOptions.setPositionAnchor(markerInGoldenGate);
    popoverOptions.setAutoPanEnabled(true);
    popoverOptions.setAutoCloseEnabled(true);
    popoverOptions.setAnchorOffset(new Point(0, 0));
    popoverOptions.setPopoverStyle(style);

    popover = googleMap3D.addPopover(popoverOptions);

    markerInGoldenGate.setClickListener(() -> {
      Log.d(getTAG(), "Marker clicked");
      if (popoverToggleCount > 5) {
        runOnUiThread(() -> {
          if (popover != null) {
            popover.remove();
          }
        });
        Log.d(getTAG(), "Popover removed");
        popoverToggleCount = 0;
      } else {
        Log.d(getTAG(), "Popover toggled");
        runOnUiThread(() -> {
          if (popover != null) {
            popover.toggle();
          }
        });
        popoverToggleCount++;
      }
    });

    Log.d(getTAG(), "Popover created");
  }

  private View createGoldenGateInfoView() {
    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

    TextView titleView = new TextView(this);
    titleView.setText("The Golden Gate Bridge");
    titleView.setTextSize(18f);
    titleView.setTextColor(Color.BLACK);
    layout.addView(titleView);

    TextView headlineView = new TextView(this);
    headlineView.setText("San Francisco, CA");
    headlineView.setTextSize(14f);
    headlineView.setTextColor(Color.DKGRAY);
    layout.addView(headlineView);

    TextView contentView = new TextView(this);
    contentView.setText("The Golden Gate Bridge is a suspension bridge\n" +
        " spanning the one-mile-wide strait connecting\n" +
        " San Francisco Bay and the Pacific Ocean.\n" +
        " The bridge was completed in 1937.");
    contentView.setTextSize(12f);
    contentView.setTextColor(Color.GRAY);
    layout.addView(contentView);

    return layout;
  }
}
