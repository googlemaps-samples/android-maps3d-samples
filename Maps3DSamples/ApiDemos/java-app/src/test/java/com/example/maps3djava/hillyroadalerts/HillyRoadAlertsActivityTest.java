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

package com.example.maps3djava.hillyroadalerts;

import static com.google.common.truth.Truth.assertThat;

import com.example.maps3d.common.AlertSeverity;
import com.example.maps3d.common.HillyRoadAlertViewModel;
import com.example.maps3d.common.HillyRoutePreset;
import com.google.android.gms.maps3d.GoogleMap3D;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

/**
 * Robolectric unit tests for {@link HillyRoadAlertsActivity} verifying map scene synchronization
 * across initial readiness and configuration changes (Activity rotation/recreation).
 */
@RunWith(RobolectricTestRunner.class)
public class HillyRoadAlertsActivityTest {

    @Test
    public void startRouteIfMapReady_afterActivityRecreationWhenViewModelAlreadyReady_rendersSceneOnNewMap()
            throws Exception {
        ActivityController<HillyRoadAlertsActivity> controller =
                Robolectric.buildActivity(HillyRoadAlertsActivity.class).setup();
        HillyRoadAlertsActivity activity = controller.get();

        HillyRoadAlertViewModel viewModel =
                (HillyRoadAlertViewModel) getField(activity, "viewModel");

        // Simulate state surviving a configuration change:
        // 1. Map was previously ready and user progressed to 250m (inside the WARNING zone)
        // 2. Old Activity's onPause() paused playback prior to recreation
        viewModel.onMapReady();
        viewModel.seekToDistance(250.0);
        viewModel.setPlaying(false);

        // Reset the Activity's per-instance rendering state to simulate a freshly recreated
        // Activity whose new Map3DView has not yet invoked onMapReady(googleMap3D).
        setField(activity, "renderedPreset", null);
        setField(activity, "lastRenderedTraveledDist", -1.0);
        setField(activity, "renderedHazardSeverity", null);
        setField(activity, "lastRenderedVehiclePos", null);

        GoogleMap3D fakeMap = createFakeGoogleMap3D();
        Method startMethod =
                HillyRoadAlertsActivity.class.getDeclaredMethod(
                        "startRouteIfMapReady", GoogleMap3D.class);
        startMethod.setAccessible(true);
        startMethod.invoke(activity, fakeMap);

        assertThat(viewModel.getCurrentState().currentDistanceMeters).isWithin(0.001).of(250.0);
        assertThat(getField(activity, "renderedPreset")).isEqualTo(HillyRoutePreset.ROLLER_COASTER);
        assertThat((Double) getField(activity, "lastRenderedTraveledDist"))
                .isWithin(0.001)
                .of(250.0);
        assertThat(getField(activity, "renderedHazardSeverity")).isEqualTo(AlertSeverity.WARNING);
        assertThat(getField(activity, "lastRenderedVehiclePos"))
                .isEqualTo(viewModel.getCurrentState().vehiclePosition);
    }

    private static GoogleMap3D createFakeGoogleMap3D() throws Exception {
        Class<GoogleMap3D> mapClass = GoogleMap3D.class;
        if (mapClass.isInterface()) {
            return createInterfaceProxy(mapClass);
        }
        GoogleMap3D instance = allocateWithoutConstructor(mapClass);
        for (Field field : mapClass.getDeclaredFields()) {
            if (field.getType().isInterface()) {
                field.setAccessible(true);
                field.set(instance, createInterfaceProxy(field.getType()));
            }
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    private static <T> T allocateWithoutConstructor(Class<T> clazz) throws Exception {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Object unsafe = unsafeField.get(null);
        Method allocateInstance = unsafeClass.getMethod("allocateInstance", Class.class);
        return (T) allocateInstance.invoke(unsafe, clazz);
    }

    @SuppressWarnings("unchecked")
    private static <T> T createInterfaceProxy(Class<T> interfaceClass) {
        return (T)
                Proxy.newProxyInstance(
                        interfaceClass.getClassLoader(),
                        new Class<?>[] {interfaceClass},
                        (proxy, method, args) -> {
                            Class<?> returnType = method.getReturnType();
                            if (returnType == void.class) {
                                return null;
                            } else if (returnType == boolean.class) {
                                return false;
                            } else if (returnType == int.class) {
                                return 0;
                            } else if (returnType == long.class) {
                                return 0L;
                            } else if (returnType == float.class) {
                                return 0f;
                            } else if (returnType == double.class) {
                                return 0.0;
                            } else if (returnType.isInterface()) {
                                return createInterfaceProxy(returnType);
                            }
                            return null;
                        });
    }

    private static Object getField(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
