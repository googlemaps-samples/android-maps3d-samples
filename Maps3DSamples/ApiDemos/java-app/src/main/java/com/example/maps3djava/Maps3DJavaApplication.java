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

package com.example.maps3djava;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.Objects;

/**
 * `Maps3DKotlinApplication` is a custom Application class for the API demo.
 *
 * This class is responsible for application-wide initialization and setup,
 * such as checking for the presence and validity of the API key during the
 * application's startup.
 *
 * It extends the [Application] class and overrides the [.onCreate]
 * method to perform these initialization tasks.
 */
public class Maps3DJavaApplication extends Application {
    private final String TAG = this.getClass().getSimpleName();

    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     * This is where application-wide initialization should be performed.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        checkApiKey();
    }

    /**
     * Checks if the API key for Google Maps is properly configured in the application's metadata.
     *
     * This method retrieves the API key from the application's metadata, specifically looking for
     * a string value associated with the key "com.google.android.geo.maps3d.API_KEY".
     * The key must be present, not blank, and not set to the placeholder value "DEFAULT_API_KEY".
     *
     * If any of these checks fail, a Toast message is displayed indicating that the API key is missing or
     * incorrectly configured, and a RuntimeException is thrown.
     */
    private void checkApiKey() {
        try {
            ApplicationInfo appInfo =
                    getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = Objects.requireNonNull(appInfo.metaData);

            String apiKey =
                    bundle.getString("com.google.android.geo.maps3d.API_KEY"); // Key name is important!

            if (apiKey == null || apiKey.isBlank() || apiKey.equals("DEFAULT_API_KEY")) {
                Toast.makeText(
                        this,
                        "API Key was not set in secrets.properties",
                        Toast.LENGTH_LONG
                ).show();
                throw new RuntimeException("API Key was not set in secrets.properties");
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Package name not found.", e);
            throw new RuntimeException("Error getting package info.", e);
        } catch (NullPointerException e) {
            Log.e(TAG, "Error accessing meta-data.", e); // Handle the case where meta-data is completely missing.
            throw new RuntimeException("Error accessing meta-data in manifest", e);
        }
    }
}