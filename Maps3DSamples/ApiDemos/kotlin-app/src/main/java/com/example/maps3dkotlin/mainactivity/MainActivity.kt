package com.example.maps3dkotlin.mainactivity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.maps3dcommon.R
import com.example.maps3dkotlin.polygons.PolygonsActivity
import com.example.maps3dkotlin.cameracontrols.CameraControlsActivity
import com.example.maps3dkotlin.hellomap.HelloMapActivity
import com.example.maps3dkotlin.markers.MarkersActivity
import com.example.maps3dkotlin.models.ModelsActivity
import com.example.maps3dkotlin.polylines.PolylinesActivity
import com.example.maps3dkotlin.theme.Maps3DSamplesTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * The main activity of the 3D Maps SDK Samples application.
 */
class MainActivity : ComponentActivity() {
    private val sampleActivities = mapOf(
        R.string.feature_title_overview_hello_3d_map to HelloMapActivity::class.java,
        R.string.feature_title_camera_controls to CameraControlsActivity::class.java,
        R.string.feature_title_markers to MarkersActivity::class.java,
        R.string.feature_title_polygons to PolygonsActivity::class.java,
        R.string.feature_title_polylines to PolylinesActivity::class.java,
        R.string.feature_title_3d_models to ModelsActivity::class.java,
    )

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Maps3DSamplesTheme {
                val coroutineScope = rememberCoroutineScope()
                val snackbarHostState = remember { SnackbarHostState() }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(title = { Text(stringResource(R.string.samples_menu_title)) })
                    },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                    SampleMenuList(
                        sampleActivities = sampleActivities,
                        modifier = Modifier.padding(innerPadding) // Pass padding to content
                    ) { activity ->
                        if (activity != null) {
                            launchSampleActivity(activity)
                        } else {
                            showSnackbar(
                                message = getString(R.string.feature_not_implemented),
                                coroutineScope = coroutineScope,
                                snackbarHostState = snackbarHostState
                            )
                        }
                    }
                }
            }
        }
    }

    private fun showSnackbar(
        message: String,
        coroutineScope: CoroutineScope,
        snackbarHostState: SnackbarHostState
    ) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(message = message)
        }
    }

    private fun launchSampleActivity(activity: Class<*>) {
        val intent = Intent(this, activity)
        startActivity(intent)
    }
}

/**
 * Composable function to display the list of samples.
 * @param sampleActivities List of sample names.
 * @param modifier Modifier to be applied to the Column.
 * @param onItemClick Lambda to be invoked when an item is clicked.
 */
@Composable
fun SampleMenuList(
    sampleActivities: Map<Int, Class<*>?>,
    modifier: Modifier = Modifier,
    onItemClick: (Class<*>?) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {

            item {
                // TODO
                Text("Need a hole demo", fontStyle = FontStyle.Italic)
            }

            item {
                // TODO
                Text("Flows demos (probably in a different) module?", fontStyle = FontStyle.Italic)
            }

            items(sampleActivities.entries.toList()) { (sampleLabelId, activity) ->
                SampleListItem(sampleLabelId = sampleLabelId, enabled = activity != null) {
                    onItemClick(activity)
                }
                HorizontalDivider()
            }
        }
    }
}

/**
 * Composable function for a single item in the sample list.
 * @param sampleLabelId The name of the sample.
 * @param onClick Lambda to be invoked when the item is clicked.
 */
@Composable
fun SampleListItem(sampleLabelId: Int, enabled: Boolean, onClick: () -> Unit) {
    Text(
        text = stringResource(sampleLabelId),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        fontSize = 18.sp,
        style = MaterialTheme.typography.bodyMedium,
        color = if (enabled) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
    )
}
