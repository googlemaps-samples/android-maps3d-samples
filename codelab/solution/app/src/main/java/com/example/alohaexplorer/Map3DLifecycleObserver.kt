package com.example.alohaexplorer

import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner
import com.google.android.gms.maps3d.Map3DView

/**
 * A lifecycle observer that automatically forwards Android Activity/Fragment
 * lifecycle events to a managed [Map3DView].
 */
class Map3DLifecycleObserver(
    private val map3DView: Map3DView,
    private val savedStateRegistryOwner: SavedStateRegistryOwner
) : DefaultLifecycleObserver {

    // Note: onCreate is typically handled manually in the Activity because it requires
    // the savedInstanceState Bundle, which DefaultLifecycleObserver doesn't inherently pass.
    // However, we can hook into the SavedStateRegistry to automate onSaveInstanceState!

    init {
        // Automatically handle onSaveInstanceState via the AndroidX SavedStateRegistry
        savedStateRegistryOwner.savedStateRegistry.registerSavedStateProvider(
            "map3d_state_provider"
        ) {
            val bundle = Bundle()
            map3DView.onSaveInstanceState(bundle)
            bundle
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        map3DView.onResume()
    }

    override fun onPause(owner: LifecycleOwner) {
        map3DView.onPause()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        map3DView.onDestroy()
        // Good citizenship: unregister the state provider when destroyed
        savedStateRegistryOwner.savedStateRegistry.unregisterSavedStateProvider("map3d_state_provider")
    }
}
