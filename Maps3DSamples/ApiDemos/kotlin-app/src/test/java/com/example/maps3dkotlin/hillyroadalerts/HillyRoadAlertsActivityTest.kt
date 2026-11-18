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

package com.example.maps3dkotlin.hillyroadalerts

import com.example.maps3d.common.AlertSeverity
import com.example.maps3d.common.HillyRoadAlertViewModel
import com.example.maps3d.common.HillyRoutePreset
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.common.truth.Truth.assertThat
import java.lang.reflect.Proxy
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

/**
 * Robolectric unit tests for [HillyRoadAlertsActivity] verifying map scene synchronization
 * across initial readiness and configuration changes (Activity rotation/recreation).
 */
@RunWith(RobolectricTestRunner::class)
class HillyRoadAlertsActivityTest {

    @Test
    fun startRouteIfMapReady_afterActivityRecreationWhenViewModelAlreadyReady_rendersSceneOnNewMap() {
        val controller = Robolectric.buildActivity(HillyRoadAlertsActivity::class.java).setup()
        val activity = controller.get()

        val viewModelField = HillyRoadAlertsActivity::class.java.getDeclaredField("viewModel\$delegate")
        viewModelField.isAccessible = true
        val viewModelLazy = viewModelField.get(activity) as Lazy<*>
        val viewModel = viewModelLazy.value as HillyRoadAlertViewModel

        // Simulate state surviving a configuration change:
        // 1. Map was previously ready and user progressed to 250m (inside the WARNING zone)
        // 2. Old Activity's onPause() paused playback prior to recreation
        viewModel.onMapReady()
        viewModel.seekToDistance(250.0)
        viewModel.setPlaying(false)

        // Reset the Activity's per-instance rendering state to simulate a freshly recreated
        // Activity whose new Map3DView has not yet invoked onMapReady(googleMap3D).
        setField(activity, "renderedPreset", null)
        setField(activity, "lastRenderedTraveledDist", -1.0)
        setField(activity, "renderedHazardSeverity", null)
        setField(activity, "lastRenderedVehiclePos", null)

        val fakeMap = createFakeGoogleMap3D()
        val startMethod = HillyRoadAlertsActivity::class.java.getDeclaredMethod(
            "startRouteIfMapReady",
            GoogleMap3D::class.java
        )
        startMethod.isAccessible = true
        startMethod.invoke(activity, fakeMap)

        // Verify the recreated Activity rendered the existing ViewModel state onto the new map
        // rather than returning early because viewModel.currentState.isMapReady was already true
        assertThat(viewModel.currentState.currentDistanceMeters).isWithin(0.001).of(250.0)
        assertThat(getField(activity, "renderedPreset")).isEqualTo(HillyRoutePreset.ROLLER_COASTER)
        assertThat(getField(activity, "lastRenderedTraveledDist") as Double).isWithin(0.001).of(250.0)
        assertThat(getField(activity, "renderedHazardSeverity")).isEqualTo(AlertSeverity.WARNING)
        assertThat(getField(activity, "lastRenderedVehiclePos"))
            .isEqualTo(viewModel.currentState.vehiclePosition)
    }

    private fun createFakeGoogleMap3D(): GoogleMap3D {
        val mapClass = GoogleMap3D::class.java
        if (mapClass.isInterface) {
            return createInterfaceProxy(mapClass)
        }
        val instance = allocateWithoutConstructor(mapClass)
        for (field in mapClass.declaredFields) {
            if (field.type.isInterface) {
                field.isAccessible = true
                field.set(instance, createInterfaceProxy(field.type))
            }
        }
        return instance
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> allocateWithoutConstructor(clazz: Class<T>): T {
        val unsafeClass = Class.forName("sun.misc.Unsafe")
        val unsafeField = unsafeClass.getDeclaredField("theUnsafe")
        unsafeField.isAccessible = true
        val unsafe = unsafeField.get(null)
        val allocateInstance = unsafeClass.getMethod("allocateInstance", Class::class.java)
        return allocateInstance.invoke(unsafe, clazz) as T
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> createInterfaceProxy(interfaceClass: Class<T>): T {
        return Proxy.newProxyInstance(
            interfaceClass.classLoader,
            arrayOf(interfaceClass)
        ) { _, method, _ ->
            val returnType = method.returnType
            when {
                returnType == Void.TYPE -> null
                returnType == Boolean::class.javaPrimitiveType -> false
                returnType == Int::class.javaPrimitiveType -> 0
                returnType == Long::class.javaPrimitiveType -> 0L
                returnType == Float::class.javaPrimitiveType -> 0f
                returnType == Double::class.javaPrimitiveType -> 0.0
                returnType.isInterface -> createInterfaceProxy(returnType)
                else -> null
            }
        } as T
    }

    private fun getField(target: Any, name: String): Any? {
        val field = target.javaClass.getDeclaredField(name)
        field.isAccessible = true
        return field.get(target)
    }

    private fun setField(target: Any, name: String, value: Any?) {
        val field = target.javaClass.getDeclaredField(name)
        field.isAccessible = true
        field.set(target, value)
    }
}
