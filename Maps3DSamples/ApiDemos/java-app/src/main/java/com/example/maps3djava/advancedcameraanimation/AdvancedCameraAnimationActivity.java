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

package com.example.maps3djava.advancedcameraanimation;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Choreographer;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import com.example.maps3d.common.AdvancedCameraAnimationViewModel;
import com.example.maps3d.common.AnimationApproach;
import com.example.maps3d.common.CameraKeyframe;
import com.example.maps3d.common.EntityPose;
import com.example.maps3d.common.HtmlUtils;
import com.example.maps3d.common.Map3DModelEntity;
import com.example.maps3d.common.SimpleFlyToMode;
import com.example.maps3d.common.TourData;
import com.example.maps3d.common.StationaryCameraTracker;
import com.example.maps3d.common.TrajectoryFlightAnimator;
import com.example.maps3d.common.EntityPose;
import com.example.maps3d.common.WorldState;

import com.example.maps3djava.R;
import com.example.maps3djava.sampleactivity.SampleBaseActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps3d.GoogleMap3D;
import com.google.android.gms.maps3d.model.AltitudeMode;
import com.google.android.gms.maps3d.model.Camera;
import com.google.android.gms.maps3d.model.FlyToOptions;
import com.google.android.gms.maps3d.model.FlyAroundOptions;
import com.google.android.gms.maps3d.model.LatLngAltitude;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;

/**
 * Java implementation of Advanced Camera Animation demo in Google Maps 3D.
 */
public class AdvancedCameraAnimationActivity extends SampleBaseActivity {

    @NonNull
    @Override
    public String getTAG() {
        return "AdvancedCameraAnimationActivity";
    }


    private AdvancedCameraAnimationViewModel viewModel;
    private final Map3DModelEntity airplaneEntity =
            new Map3DModelEntity(TourData.AIRPLANE_MODEL_ID, TourData.AIRPLANE_MODEL_URL, AltitudeMode.ABSOLUTE);

    private CardView controlsCard;
    private MaterialButton btnPlayPause;
    private MaterialButton btnReset;
    private MaterialButton btnCollapseToggle;
    private TextView tvTourStatus;
    private LinearLayout collapsibleContent;
    private MaterialButton btnSelectApproach;
    private com.google.android.material.card.MaterialCardView cardKeyframeTourStep;
    private TextView tvKeyframeStepBadge;
    private TextView tvKeyframeStepDesc;
    private com.google.android.material.progressindicator.LinearProgressIndicator progressKeyframeStep;
    private TextView tvStepDetail;
    private LinearLayout layoutSimpleFlyToOptions;
    private ChipGroup chipGroupSimpleFlyToMode;

    private boolean isControlsCollapsed = false;
    private final Handler autoFadeHandler = new Handler(Looper.getMainLooper());
    private final Runnable autoFadeRunnable = () ->
            controlsCard.animate().alpha(0.35f).setDuration(400L).start();

    private Choreographer.FrameCallback frameCallback;
    private boolean isTourRunning = false;
    private int currentKeyframeIndex = 0;

    @NonNull
    @Override
    public Camera getInitialCamera() {
        LatLng start = TourData.AIRPLANE_FLIGHT_PATH.get(0);
        return new Camera(
                new LatLngAltitude(start.latitude, start.longitude, 250.0),
                105.0,
                65.0,
                0.0,
                600.0
        );
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (snapshotButton != null) snapshotButton.setVisibility(View.GONE);
        if (recenterButton != null) recenterButton.setVisibility(View.GONE);

        MaterialToolbar topBar = findViewById(com.example.maps3dcommon.R.id.top_bar);
        if (topBar != null) {
            topBar.setTitle(com.example.maps3dcommon.R.string.aerial_tour_title);
            topBar.setSubtitle(com.example.maps3dcommon.R.string.framework_java_views);
        }

        viewModel = new ViewModelProvider(this).get(AdvancedCameraAnimationViewModel.class);

        setupCustomControls();
        observeViewModel();
        resetAutoFadeTimer();
    }

    private void setupCustomControls() {
        ViewGroup rootLayout = findViewById(com.example.maps3dcommon.R.id.map_container);
        View customView = getLayoutInflater().inflate(
                com.example.maps3dcommon.R.layout.control_panel_advanced_animation,
                rootLayout,
                false
        );
        rootLayout.addView(customView);

        controlsCard = customView.findViewById(com.example.maps3dcommon.R.id.control_panel);
        TextView tvFrameworkSubtitle = customView.findViewById(com.example.maps3dcommon.R.id.tv_framework_subtitle);
        if (tvFrameworkSubtitle != null) {
            tvFrameworkSubtitle.setText("Java Views");
            tvFrameworkSubtitle.setVisibility(View.VISIBLE);
        }
        LinearLayout headerTitleBar = customView.findViewById(com.example.maps3dcommon.R.id.header_title_bar);
        MaterialButton btnHelp = customView.findViewById(com.example.maps3dcommon.R.id.btn_help);
        btnCollapseToggle = customView.findViewById(com.example.maps3dcommon.R.id.btn_collapse_toggle);
        btnPlayPause = customView.findViewById(com.example.maps3dcommon.R.id.btn_play_pause);
        btnReset = customView.findViewById(com.example.maps3dcommon.R.id.btn_reset);
        tvTourStatus = customView.findViewById(com.example.maps3dcommon.R.id.tv_tour_status);
        collapsibleContent = customView.findViewById(com.example.maps3dcommon.R.id.collapsible_content);
        btnSelectApproach = customView.findViewById(com.example.maps3dcommon.R.id.btn_select_approach);
        tvStepDetail = customView.findViewById(com.example.maps3dcommon.R.id.tv_step_detail);
        layoutSimpleFlyToOptions = customView.findViewById(com.example.maps3dcommon.R.id.layout_simple_fly_to_options);
        cardKeyframeTourStep = customView.findViewById(com.example.maps3dcommon.R.id.card_keyframe_tour_step);
        tvKeyframeStepBadge = customView.findViewById(com.example.maps3dcommon.R.id.tv_keyframe_step_badge);
        tvKeyframeStepDesc = customView.findViewById(com.example.maps3dcommon.R.id.tv_keyframe_step_description);
        progressKeyframeStep = customView.findViewById(com.example.maps3dcommon.R.id.progress_keyframe_step);
        chipGroupSimpleFlyToMode = customView.findViewById(com.example.maps3dcommon.R.id.chip_group_simple_fly_to_mode);

        headerTitleBar.setOnClickListener(v -> {
            toggleControlsCollapse();
            resetAutoFadeTimer();
        });

        btnCollapseToggle.setOnClickListener(v -> {
            toggleControlsCollapse();
            resetAutoFadeTimer();
        });

        btnPlayPause.setOnClickListener(v -> {
            resetAutoFadeTimer();
            if (viewModel.getCurrentState().isPlaying()) {
                stopAnimationLoops();
                viewModel.pause();
            } else {
                startSelectedApproach();
            }
        });

        btnReset.setOnClickListener(v -> {
            resetAutoFadeTimer();
            stopAnimationLoops();
            viewModel.resetTour();
            if (googleMap3D != null) {
                Camera targetCam = (viewModel.getCurrentState().getSelectedApproach() == AnimationApproach.KEYFRAME_TOUR) ? TourData.OVERVIEW_CAMERA : getInitialCamera();
                    googleMap3D.setCamera(targetCam);
            }
        });

        btnHelp.setOnClickListener(v -> {
            showHelpDialog();
            resetAutoFadeTimer();
        });

        btnSelectApproach.setOnClickListener(v -> {
            resetAutoFadeTimer();
            showApproachMenu(v);
        });

        chipGroupSimpleFlyToMode.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            SimpleFlyToMode mode = (id == com.example.maps3dcommon.R.id.chip_fly_to_midpoint)
                    ? SimpleFlyToMode.MIDPOINT_JUMP
                    : SimpleFlyToMode.SYNCHRONIZED_FLIGHT;
            viewModel.setSimpleFlyToMode(mode);
            resetAutoFadeTimer();
        });

        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null) return false;
                float deltaY = e2.getY() - e1.getY();
                if (Math.abs(deltaY) > 50 && Math.abs(velocityY) > 100) {
                    if (deltaY > 0 && !isControlsCollapsed) {
                        toggleControlsCollapse();
                    } else if (deltaY < 0 && isControlsCollapsed) {
                        toggleControlsCollapse();
                    }
                    return true;
                }
                return false;
            }
        });

        controlsCard.setOnTouchListener((v, event) -> {
            resetAutoFadeTimer();
            gestureDetector.onTouchEvent(event);
            return false;
        });
    }

    private void observeViewModel() {
        viewModel.getLiveData().observe(this, state -> {
            if (state == null) return;
            tvTourStatus.setText(state.getStatusText());
            btnPlayPause.setIconResource(
                    state.isPlaying()
                            ? com.example.maps3dcommon.R.drawable.pause_24px
                            : com.example.maps3dcommon.R.drawable.play_arrow_24px
            );

            // Synchronize approach button label & UI
            btnSelectApproach.setText(state.getSelectedApproach().getTitle());
            layoutSimpleFlyToOptions.setVisibility(
                    state.getSelectedApproach() == AnimationApproach.SIMPLE_FLY_TO ? View.VISIBLE : View.GONE
            );

            boolean isKeyframeTour = state.getSelectedApproach() == AnimationApproach.KEYFRAME_TOUR;
            cardKeyframeTourStep.setVisibility(isKeyframeTour ? View.VISIBLE : View.GONE);
            if (isKeyframeTour) {
                tvKeyframeStepBadge.setText(!state.getStepTitle().isEmpty() ? state.getStepTitle() : "Step " + (state.getCurrentStepIndex() + 1) + " of " + state.getTotalSteps());
                tvKeyframeStepDesc.setText(state.getStepDescription());
                progressKeyframeStep.setMax(state.getTotalSteps());
                progressKeyframeStep.setProgress(state.getCurrentStepIndex() + 1);
            }

            // Update detail explanation text
            updateApproachUI(state.getSelectedApproach());

            // Synchronize sub-mode chip selection
            int targetSubModeChipId = (state.getSimpleFlyToMode() == SimpleFlyToMode.MIDPOINT_JUMP)
                    ? com.example.maps3dcommon.R.id.chip_fly_to_midpoint
                    : com.example.maps3dcommon.R.id.chip_fly_to_synchronized;
            if (chipGroupSimpleFlyToMode.getCheckedChipId() != targetSubModeChipId) {
                chipGroupSimpleFlyToMode.check(targetSubModeChipId);
            }

            if (googleMap3D != null) {
                if (state.getSelectedApproach() == AnimationApproach.DISPATCHER_FRAME_LOOP ||
                        state.getSelectedApproach() == AnimationApproach.ORBIT_360_SPIN) {
                    googleMap3D.setCamera(state.getCamera());
                }
            }

            EntityPose pose = state.getEntityPose(TourData.AIRPLANE_MODEL_ID);
            if (pose != null) {
                airplaneEntity.applyPose(pose, googleMap3D);
            }
        });
    }

    @Override
    public void onMap3DViewReady(@NonNull GoogleMap3D googleMap3D) {
        super.onMap3DViewReady(googleMap3D);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isDestroyed() && !isFinishing() && this.googleMap3D != null) {
                Camera targetCam = (viewModel.getCurrentState().getSelectedApproach() == AnimationApproach.KEYFRAME_TOUR)
                        ? TourData.OVERVIEW_CAMERA
                        : getInitialCamera();
                this.googleMap3D.setCamera(targetCam);
                EntityPose initialPose = viewModel.getCurrentState().getEntityPose(TourData.AIRPLANE_MODEL_ID);
                if (initialPose != null) {
                    airplaneEntity.attach(this.googleMap3D, initialPose);
                }
            }
        }, 350L);
    }

    private void resetAndRestartTour() {
        if (googleMap3D != null) {
            Camera targetCam = (viewModel.getCurrentState().getSelectedApproach() == AnimationApproach.KEYFRAME_TOUR) ? TourData.OVERVIEW_CAMERA : getInitialCamera();
                    googleMap3D.setCamera(targetCam);
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isDestroyed() && !isFinishing()) {
                startSelectedApproach();
            }
        }, 400L);
    }

    private void startSelectedApproach() {
        if (googleMap3D == null) return;
        stopAnimationLoops();
        viewModel.play();

        AnimationApproach approach = viewModel.getCurrentState().getSelectedApproach();
        if (approach == AnimationApproach.SIMPLE_FLY_TO) {
            runSimpleFlyTo(googleMap3D);
        } else if (approach == AnimationApproach.KEYFRAME_TOUR) {
            runKeyframeTour(googleMap3D);
        } else if (approach == AnimationApproach.DISPATCHER_FRAME_LOOP) {
            runFrameDispatcherLoop();
        } else if (approach == AnimationApproach.ORBIT_360_SPIN) {
            runContinuousOrbitLoop();
        }
    }

    private void runSimpleFlyTo(GoogleMap3D map) {
        isTourRunning = true;
        LatLng target = TourData.AIRPLANE_FLIGHT_PATH.get(TourData.AIRPLANE_FLIGHT_PATH.size() - 1);
        Camera targetCam = new Camera(
                new LatLngAltitude(target.latitude, target.longitude, 250.0),
                285.0, // Facing back toward Golden Gate Bridge to watch the plane fly in
                65.0,
                0.0,
                600.0
        );

        FlyToOptions options = new FlyToOptions(targetCam, 5000);

        frameCallback = new Choreographer.FrameCallback() {
            private long lastNanos = 0L;
            @Override
            public void doFrame(long frameTimeNanos) {
                if (lastNanos > 0L) {
                    double dt = (frameTimeNanos - lastNanos) / 1_000_000_000.0;
                    viewModel.tick(Math.max(0.001, Math.min(0.1, dt)));
                }
                lastNanos = frameTimeNanos;
                if (viewModel.getCurrentState().isPlaying() && isTourRunning) {
                    Choreographer.getInstance().postFrameCallback(this);
                }
            }
        };
        Choreographer.getInstance().postFrameCallback(frameCallback);

        map.setCameraAnimationEndListener(() -> {
            map.setCameraAnimationEndListener(null);
            stopAnimationLoops();
            viewModel.onNativeCameraAnimationFinished();
        });
        map.flyCameraTo(options);
    }

    private void runKeyframeTour(GoogleMap3D map) {
        isTourRunning = true;
        currentKeyframeIndex = 0;
        executeNextKeyframeStep(map);
    }

    private void executeNextKeyframeStep(GoogleMap3D map) {
        List<CameraKeyframe> tour = TourData.SAN_FRANCISCO_TOUR;
        if (!isTourRunning || currentKeyframeIndex >= tour.size() || !viewModel.getCurrentState().isPlaying()) {
            stopAnimationLoops();
            viewModel.onNativeCameraAnimationFinished();
            return;
        }

        viewModel.setKeyframeStep(currentKeyframeIndex);
        CameraKeyframe step = tour.get(currentKeyframeIndex);

        if (step instanceof CameraKeyframe.FlyTo) {
            CameraKeyframe.FlyTo flyStep = (CameraKeyframe.FlyTo) step;
            FlyToOptions options = new FlyToOptions(flyStep.getTargetCamera(), (int) flyStep.getDurationMs());
            map.setCameraAnimationEndListener(() -> {
                map.setCameraAnimationEndListener(null);
                currentKeyframeIndex++;
                executeNextKeyframeStep(map);
            });
            map.flyCameraTo(options);
        } else if (step instanceof CameraKeyframe.DwellPause) {
            CameraKeyframe.DwellPause dwellStep = (CameraKeyframe.DwellPause) step;
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                currentKeyframeIndex++;
                executeNextKeyframeStep(map);
            }, dwellStep.getDurationMs());
        } else if (step instanceof CameraKeyframe.FlyAround) {
            CameraKeyframe.FlyAround flyAround = (CameraKeyframe.FlyAround) step;
            FlyAroundOptions options = new FlyAroundOptions(flyAround.getCenterCamera(), (int) flyAround.getDurationMs(), (float) flyAround.getRounds());
            map.setCameraAnimationEndListener(() -> {
                map.setCameraAnimationEndListener(null);
                currentKeyframeIndex++;
                executeNextKeyframeStep(map);
            });
            map.flyCameraAround(options);
        } else if (step instanceof CameraKeyframe.StationaryTrackingFlight) {
            CameraKeyframe.StationaryTrackingFlight trackingStep = (CameraKeyframe.StationaryTrackingFlight) step;
            FlyToOptions toVantage = new FlyToOptions(trackingStep.getObservationCamera(), 2000);
            map.setCameraAnimationEndListener(() -> {
                map.setCameraAnimationEndListener(null);
                if (!isTourRunning || !viewModel.getCurrentState().isPlaying()) return;
                StationaryCameraTracker tracker = StationaryCameraTracker.Companion.fromInitialCamera(trackingStep.getObservationCamera());
                TrajectoryFlightAnimator flightAnimator = new TrajectoryFlightAnimator(trackingStep.getFlightPath(), 250.0, 0.08);
                long startTime = System.currentTimeMillis();
                Handler trackHandler = new Handler(Looper.getMainLooper());
                Runnable trackRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (!isTourRunning || !viewModel.getCurrentState().isPlaying()) return;
                        long elapsed = System.currentTimeMillis() - startTime;
                        EntityPose targetPose = flightAnimator.update(elapsed, trackingStep.getDurationMs());
                        Camera trackingCam = tracker.computeTrackingCamera(targetPose);

                        viewModel.updateAirplanePose(targetPose);
                        airplaneEntity.applyPose(targetPose, map);
                        map.setCamera(trackingCam);

                        if (!flightAnimator.isFinished(elapsed, trackingStep.getDurationMs())) {
                            trackHandler.postDelayed(this, 16L);
                        } else {
                            EntityPose finalPose = flightAnimator.update(trackingStep.getDurationMs(), trackingStep.getDurationMs());
                            viewModel.updateAirplanePose(finalPose);
                            airplaneEntity.applyPose(finalPose, map);

                            currentKeyframeIndex++;
                            executeNextKeyframeStep(map);
                        }
                    }
                };
                trackHandler.post(trackRunnable);
            });
            map.flyCameraTo(toVantage);
        }
    }

    private void runFrameDispatcherLoop() {
        isTourRunning = true;
        frameCallback = new Choreographer.FrameCallback() {
            private long lastNanos = 0L;
            @Override
            public void doFrame(long frameTimeNanos) {
                if (lastNanos > 0L) {
                    double dt = (frameTimeNanos - lastNanos) / 1_000_000_000.0;
                    viewModel.tick(Math.max(0.001, Math.min(0.1, dt)));
                }
                lastNanos = frameTimeNanos;
                if (viewModel.getCurrentState().isPlaying() && isTourRunning) {
                    Choreographer.getInstance().postFrameCallback(this);
                }
            }
        };
        Choreographer.getInstance().postFrameCallback(frameCallback);
    }

    private void runContinuousOrbitLoop() {
        isTourRunning = true;
        frameCallback = new Choreographer.FrameCallback() {
            private long lastNanos = 0L;
            @Override
            public void doFrame(long frameTimeNanos) {
                if (lastNanos > 0L) {
                    double dt = (frameTimeNanos - lastNanos) / 1_000_000_000.0;
                    viewModel.tick(Math.max(0.001, Math.min(0.1, dt)));
                }
                lastNanos = frameTimeNanos;
                if (viewModel.getCurrentState().isPlaying() && isTourRunning) {
                    Choreographer.getInstance().postFrameCallback(this);
                }
            }
        };
        Choreographer.getInstance().postFrameCallback(frameCallback);
    }

    private void stopAnimationLoops() {
        isTourRunning = false;
        if (frameCallback != null) {
            Choreographer.getInstance().removeFrameCallback(frameCallback);
            frameCallback = null;
        }
        if (googleMap3D != null) {
            googleMap3D.setCameraAnimationEndListener(null);
            googleMap3D.stopCameraAnimation();
        }
    }

    private void toggleControlsCollapse() {
        isControlsCollapsed = !isControlsCollapsed;
        collapsibleContent.setVisibility(isControlsCollapsed ? View.GONE : View.VISIBLE);
        btnCollapseToggle.setIconResource(
                isControlsCollapsed
                        ? com.example.maps3dcommon.R.drawable.expand_less_24px
                        : com.example.maps3dcommon.R.drawable.expand_more_24px
        );
    }

    private void updateApproachUI(AnimationApproach approach) {
        layoutSimpleFlyToOptions.setVisibility(
                approach == AnimationApproach.SIMPLE_FLY_TO ? View.VISIBLE : View.GONE
        );
        if (approach == AnimationApproach.SIMPLE_FLY_TO) {
            tvStepDetail.setText("Native asynchronous SDK flight transition directly to Coit Tower.");
        } else if (approach == AnimationApproach.KEYFRAME_TOUR) {
            tvStepDetail.setText("Declarative 5-step sequence: Swoop FlyTo → Dwell Pause → 360° Orbit → Stationary Tracking Flight → Final FlyTo.");
        } else if (approach == AnimationApproach.DISPATCHER_FRAME_LOOP) {
            tvStepDetail.setText("Continuous 400 m/s flight synced to hardware VSYNC display frames.");
        } else if (approach == AnimationApproach.ORBIT_360_SPIN) {
            tvStepDetail.setText("Continuous 360° orbital camera rotation around Golden Gate Bridge.");
        }
    }

    private void resetAutoFadeTimer() {
        controlsCard.animate().alpha(1.0f).setDuration(150L).start();
        autoFadeHandler.removeCallbacks(autoFadeRunnable);
        autoFadeHandler.postDelayed(autoFadeRunnable, 3500L);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        resetAutoFadeTimer();
        return super.dispatchTouchEvent(ev);
    }

    private void showApproachMenu(View anchor) {
        androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(this, anchor);
        AnimationApproach[] approaches = AnimationApproach.values();
        for (int i = 0; i < approaches.length; i++) {
            popup.getMenu().add(0, i, i, approaches[i].getTitle());
        }
        popup.setOnMenuItemClickListener(item -> {
            resetAutoFadeTimer();
            int index = item.getItemId();
            if (index >= 0 && index < approaches.length) {
                stopAnimationLoops();
                viewModel.setApproach(approaches[index]);
                viewModel.resetTour();
                if (googleMap3D != null) {
                    Camera targetCam = (viewModel.getCurrentState().getSelectedApproach() == AnimationApproach.KEYFRAME_TOUR) ? TourData.OVERVIEW_CAMERA : getInitialCamera();
                    googleMap3D.setCamera(targetCam);
                }
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void showHelpDialog() {
        View dialogView = getLayoutInflater().inflate(com.example.maps3dcommon.R.layout.dialog_help_advanced_animation, null);
        TextView tvContent = dialogView.findViewById(com.example.maps3dcommon.R.id.tv_help_html_content);
        if (tvContent != null) {
            tvContent.setText(HtmlUtils.loadRawHtml(this, com.example.maps3dcommon.R.raw.help_advanced_animation));
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(com.example.maps3dcommon.R.string.help_dialog_advanced_animation_title)
                .setView(dialogView)
                .setPositiveButton(com.example.maps3dcommon.R.string.help_dialog_ok, (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAnimationLoops();
        viewModel.pause();
        autoFadeHandler.removeCallbacks(autoFadeRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        airplaneEntity.detach();
    }
}
