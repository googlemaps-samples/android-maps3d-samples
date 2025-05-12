// Copyright 2025 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.advancedmaps3dsamples.scenarios

import com.example.advancedmaps3dsamples.R

const val PLANE_URL = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/Airplane.glb"
const val PLANE_SCALE = 0.05
const val SAUCER_URL = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/UFO.glb"

val scenarios =
  listOf(
      createScenario(
        name = "tower_bridge",
        titleId = R.string.scenarios_tower_bridge,
        initialState =
          "mode=satellite;camera=lat=51.5057832,lng=-0.0751902,alt=5.6035,hdg=-16.36154,tilt=0,range=20000",
        animationString =
          "delay=dur=2000;" +
            "flyTo=lat=51.5057832,lng=-0.0751902,alt=5.6035,hdg=-16.36154,tilt=65,range=564,dur=3500;" +
            "delay=dur=1500;" +
            "flyAround=lat=51.5057832,lng=-0.0751902,alt=5.6035,hdg=-16.36154,tilt=65,range=564,dur=5000,count=1",
      ),

      createScenario(
        name = "nyc_hybrid",
        titleId = R.string.scenarios_nyc_hybrid,
        // Initial state: Wide view over North America
        initialState =
          "mode=hybrid;camera=lat=51.4045642,lng=-94.023074,alt=100,hdg=0.0,tilt=0.0,range=15000000",
        // Animation: Initial delay, fly to ESB, pause, zoom in, pause, pan right, pause, pan left & tilt up, pause, zoom out slightly, final pause.
        animationString =
          "delay=dur=2000;" + // Initial 1s delay
            // Fly to a viewpoint near the Empire State Building
            "flyTo=lat=40.748392,lng=-73.986060,alt=174.1,hdg=26.3,tilt=67,range=3977,dur=4500;" +
            "delay=dur=2000;" + // Pause for 1s after arriving

            // --- Start replacing flyAround with interactions ---
            // 1. Zoom In closer to the ESB
            "flyTo=lat=40.748392,lng=-73.986060,alt=174.1,hdg=26.3,tilt=67,range=1000,dur=1500;" +
            "delay=dur=750;" +  // Short pause after zooming in

            // 2. Pan Right (change heading)
            "flyTo=lat=40.748392,lng=-73.986060,alt=174.1,hdg=75.0,tilt=67,range=1000,dur=2000;" +
            "delay=dur=750;" +  // Short pause after panning right

            // 3. Pan Left and Tilt Up slightly (change heading and tilt)
            "flyTo=lat=40.748392,lng=-73.986060,alt=174.1,hdg=0.0,tilt=60,range=1000,dur=2000;" +
            "delay=dur=750;" +  // Short pause after panning left/tilting

            // 4. Zoom Out slightly
            "flyTo=lat=40.748392,lng=-73.986060,alt=174.1,hdg=0.0,tilt=60,range=2500,dur=1500;" +
            // --- End of new interactions ---

            "delay=dur=1500", // Final pause before scenario end overlay
      ),

      createScenario(
        name = "nyc_satellite",
        titleId = R.string.scenarios_nyc_satellite,
        // Initial state: Wide view over North America
        initialState =
          "mode=satellite;camera=lat=51.4045642,lng=-94.023074,alt=100,hdg=0.0,tilt=0.0,range=15000000",
        // Animation: Initial delay, fly to ESB, pause, zoom in, pause, pan right, pause, pan left & tilt up, pause, zoom out slightly, final pause.
        animationString =
          "delay=dur=2000;" + // Initial 1s delay
            // Fly to a viewpoint near the Empire State Building
            "flyTo=lat=40.748392,lng=-73.986060,alt=174.1,hdg=26.3,tilt=67,range=3977,dur=4500;" +
            "delay=dur=2000;" + // Pause for 1s after arriving

            // --- Start replacing flyAround with interactions ---
            // 1. Zoom In closer to the ESB
            "flyTo=lat=40.748392,lng=-73.986060,alt=174.1,hdg=26.3,tilt=67,range=1000,dur=1500;" +
            "delay=dur=750;" +  // Short pause after zooming in

            // 2. Pan Right (change heading)
            "flyTo=lat=40.748392,lng=-73.986060,alt=174.1,hdg=75.0,tilt=67,range=1000,dur=2000;" +
            "delay=dur=750;" +  // Short pause after panning right

            // 3. Pan Left and Tilt Up slightly (change heading and tilt)
            "flyTo=lat=40.748392,lng=-73.986060,alt=174.1,hdg=0.0,tilt=60,range=1000,dur=2000;" +
            "delay=dur=750;" +  // Short pause after panning left/tilting

            // 4. Zoom Out slightly
            "flyTo=lat=40.748392,lng=-73.986060,alt=174.1,hdg=0.0,tilt=60,range=2500,dur=1500;" +
            // --- End of new interactions ---

            "delay=dur=1500", // Final pause before scenario end overlay
      ),
      createScenario(
        name = "camera",
        titleId = R.string.scenarios_camera_control,
        initialState =
          "mode=satellite;camera=lat=47.557714,lng=10.749557,alt=988.6,hdg=0,tilt=55,range=723",
        animationString =
          "delay=dur=2000", // This scenario runs custom code, not parsed animation string
      ),
      createScenario(
        name = "fly_to",
        titleId = R.string.scenarios_bondi_to_eye,
        initialState =
          "mode=satellite;camera=lat=-33.891984,lng=151.273785,alt=13.3,hdg=274.5,tilt=71,range=3508",
        animationString =
          "delay=dur=2000;" +
            "flyTo=lat=-33.868670,lng=151.204183,alt=39.6,hdg=293.8,tilt=69,range=1512,dur=2500;" +
            "delay=dur=2000",
      ),
      createScenario(
        name = "fly_around",
        titleId = R.string.scenarios_delicate_arch,
        initialState =
          "mode=satellite;camera=lat=36.10145879,lng=-112.10555998,alt=774.39,hdg=33.198,tilt=74.036,range=9180.62",
        animationString =
          "delay=dur=3000;" +
            "flyTo=lat=38.743502,lng=-109.499374,alt=1467,hdg=-10.4,tilt=58.1,range=138.2,dur=3500;" +
            "delay=dur=2000;" +
            "flyAround=lat=38.743502,lng=-109.499374,alt=1467,hdg=-10.4,tilt=58.1,range=138.2,dur=6000,count=2;" +
            "delay=dur=2000",
      ),
      createScenario(
        name = "markers",
        titleId = R.string.scenarios_markers,
        initialState =
          "mode=satellite;camera=lat=52.51974795,lng=13.40715553,alt=150,hdg=252.7,tilt=79,range=1500",
        animationString =
          "delay=dur=2000;" +
            "flyTo=lat=52.522255,lng=13.405010,alt=84.0,hdg=312.8,tilt=66,range=1621,dur=2000;" +
            "delay=dur=3000",
        markers =
          "lat=52.519605780912585,lng=13.406867190588198,alt=150,label= ,altMode=absolute;" +
            "lat=52.519882191069016,lng=13.407410777254293,alt=50,label= ,altMode=relative_to_ground;" +
            "lat=52.52027645136134,lng=13.408271658592406,alt=5,label= ,altMode=clamp_to_ground;" +
            "lat=52.520835071144226,lng=13.409426847943774,alt=10,label= ,altMode=relative_to_mesh;"
      ),
      createScenario(
        name = "model",
        titleId = R.string.scenarios_model,
        initialState =
          "mode=satellite;camera=lat=47.133971,lng=11.333161,alt=2200,hdg=221.4,tilt=25,range=30000",
        animationString =
          // Initial delay
          "delay=dur=1000;" +
          "flyTo=lat=47.133971,lng=11.333161,alt=2200,hdg=221.4,tilt=65,range=1200,dur=3500;" +
          "flyAround=lat=47.133971,lng=11.333161,alt=2200,hdg=221.4,tilt=65,range=1200,dur=3500,count=0.5;" +
          "delay=dur=1000",
        // Define the model(s) for this scenario
        models =
          "id=plane_main,lat=47.133971,lng=11.333161,alt=2200,url=$PLANE_URL,altMode=absolute,scaleX=$PLANE_SCALE,scaleY=$PLANE_SCALE,scaleZ=$PLANE_SCALE,hdg=41.5,tilt=-90,roll=0;"
      ),
      createScenario(
        name = "polyline",
        titleId = R.string.scenarios_polyline,
        initialState =
          "mode=satellite;camera=lat=41.886251,lng=-87.628896,alt=367.3,hdg=190.5,tilt=71,range=19962",
        animationString =
            "delay=dur=1000;" +
              "flyTo=lat=41.901229,lng=-87.621649,alt=179.6,hdg=169.0,tilt=71,range=4145,dur=2500;" +
            "delay=dur=1000",
        polylines =
          "o`v~FnsxuOrAAKy\\S{@cPrJqF`Dc@d@Wb@Uv@It@S~GQ|AYjA]dAo@jAu@z@c@`@}@f@}A`@{PpCkQlCgBJoILW]mDc@o@@wATa@Pg@t@",
      ),
    )
    .associateBy { it.name }
