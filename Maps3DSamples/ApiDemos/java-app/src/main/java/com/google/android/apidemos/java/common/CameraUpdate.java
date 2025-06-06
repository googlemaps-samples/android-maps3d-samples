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

package com.google.android.apidemos.java.common;

// Placeholder imports
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.FlyToOptions;
import com.google.android.gms.maps3d.FlyAroundOptions;
import com.google.android.gms.maps3d.Camera;

public abstract class CameraUpdate {
    public abstract void invoke(GoogleMap3D controller);

    public static class FlyTo extends CameraUpdate {
        private final FlyToOptions options;

        public FlyTo(FlyToOptions options) {
            this.options = options;
        }

        @Override
        public void invoke(GoogleMap3D controller) {
            controller.flyTo(options);
        }
    }

    public static class FlyAround extends CameraUpdate {
        private final FlyAroundOptions options;

        public FlyAround(FlyAroundOptions options) {
            this.options = options;
        }

        @Override
        public void invoke(GoogleMap3D controller) {
            controller.flyAround(options);
        }
    }

    public static class Move extends CameraUpdate {
        private final Camera camera;

        public Move(Camera camera) {
            this.camera = camera;
        }

        @Override
        public void invoke(GoogleMap3D controller) {
            controller.move(camera);
        }
    }
}
