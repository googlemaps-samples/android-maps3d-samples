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

package com.example.advancedmaps3dsamples.scenarios

import com.example.advancedmaps3dsamples.R
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.ktx.utils.latLngListEncode

const val PLANE_URL = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/Airplane.glb"
const val PLANE_SCALE = 0.05
const val SAUCER_URL = "https://storage.googleapis.com/gmp-maps-demos/p3d-map/assets/UFO.glb"

const val hawaiiCenter = "lat=21.273424,lng=-157.821186,alt=16,hdg=306,tilt=58,range=10000"

val hawaiiRoute = """
    21.30617, -157.85958
    21.30615, -157.85961
    21.30576, -157.85918
    21.30551, -157.85882
    21.30448, -157.85710
    21.30407, -157.85629
    21.30382, -157.85577
    21.30343, -157.85549
    21.30318, -157.85536
    21.30259, -157.85511
    21.30199, -157.85480
    21.30171, -157.85469
    21.30123, -157.85445
    21.30088, -157.85423
    21.29973, -157.85327
    21.29931, -157.85293
    21.29897, -157.85270
    21.29785, -157.85183
    21.29740, -157.85142
    21.29703, -157.85097
    21.29674, -157.85053
    21.29639, -157.84984
    21.29572, -157.84831
    21.29492, -157.84660
    21.29465, -157.84589
    21.29423, -157.84504
    21.29319, -157.84268
    21.29075, -157.83721
    21.29064, -157.83683
    21.29060, -157.83656
    21.29058, -157.83620
    21.29057, -157.83572
    21.28936, -157.83510
    21.28713, -157.83389
    21.28559, -157.83293
    21.28470, -157.83238
    21.28425, -157.83211
    21.28295, -157.83133
    21.28146, -157.83057
    21.28009, -157.82978
    21.27935, -157.82919
    21.27647, -157.82600
    21.27610, -157.82561
    21.27538, -157.82494
    21.27504, -157.82460
    21.27496, -157.82455
    21.27153, -157.82277
    21.27121, -157.82264
    21.27113, -157.82257
    21.27091, -157.82245
    21.27028, -157.82183
    21.27004, -157.82157
    21.26962, -157.82097
    21.26940, -157.81891
    21.26913, -157.81587
    21.26908, -157.81475
    21.26907, -157.81322
    21.26914, -157.81276
    21.26959, -157.81132
    21.26980, -157.80880
    21.26980, -157.80831
    21.26978, -157.80783
    21.26974, -157.80764
    21.26965, -157.80679
    21.26958, -157.80591
    21.26957, -157.80413
    21.26966, -157.80309
    21.26966, -157.80289
    21.26960, -157.80262
    21.26957, -157.80252
    21.26947, -157.80231
    21.26933, -157.80213
    21.26905, -157.80187
    21.26882, -157.80159
    21.26844, -157.80091
    21.26814, -157.80052
    21.26784, -157.80019
    21.26761, -157.79988
    21.26725, -157.80014
    21.26720, -157.80014
    21.26712, -157.80009
    21.26695, -157.79994
    21.26688, -157.79989
    21.26670, -157.79981
    21.26633, -157.79975
    21.26604, -157.79973
    21.26533, -157.79960
    21.26502, -157.79954
    21.26494, -157.79952
    21.26476, -157.79938
    21.26457, -157.79935
    21.26440, -157.79936
    21.26427, -157.79943
    21.26423, -157.79947
    21.26417, -157.79957
    21.26394, -157.80021
    21.26325, -157.80217
    21.26287, -157.80324
    21.26286, -157.80343
    21.26289, -157.80359
    21.26401, -157.80579
""".trimIndent().split("\n")
    .map {
        val (lat, lng) = it.split(",").map(String::toDouble)
        LatLng(lat, lng)
    }
    .latLngListEncode()

// Encoded polyline
// qj`aCj}nb]BDlAuAp@gAlEwIpAaDp@gBlAw@p@YtBq@vB}@v@U~Ao@dAk@dF_ErAcAbAm@~EmDxAqAhAyAx@wAdAiCdCqH~CuIt@mCrAiDnEwMfNea@TkAFu@BgA@_BpF{B|LqFrH_EpDmBxAu@bG{ChHwCpG}CrCuB~P}RhAmAnCeCbAcANIlTcJ~@YNMj@W|B{Bn@s@rAwBj@{Kt@_RH_F@qHM{AyA_Hi@wN?aBB_BFe@PiDLoD@cJQoE?g@Ju@DSRi@Zc@v@s@l@w@jAgCz@mAz@aAl@}@fAr@H?NI`@]LIb@OhAKx@ClCY|@KNCb@[d@E`@@XLFFJRl@~BhCfKjAtE@d@E^_FvL

val denverPolygon = """
    outer=39.7508987:-104.9565381|
          39.7502883:-104.9565489|
          39.7501976:-104.9563557|
          39.7501481:-104.955594|
          39.7499171:-104.9553043|
          39.7495872:-104.9551648|
          39.7492407:-104.954961|
          39.7489685:-104.9548859|
          39.7484488:-104.9548966|
          39.7481189:-104.9548859|
          39.7479539:-104.9547679|
          39.7479209:-104.9544567|
          39.7476487:-104.9535341|
          39.7475085:-104.9525792|
          39.7474095:-104.9519247|
          39.747525:-104.9513776|
          39.7476734:-104.9511844|
          39.7478137:-104.9506265|
          39.7477559:-104.9496395|
          39.7477477:-104.9486203|
          39.7478467:-104.9475796|
          39.7482344:-104.9465818|
          39.7486138:-104.9457878|
          39.7491005:-104.9454874|
          39.7495789:-104.945938|
          39.7500491:-104.9466998|
          39.7503213:-104.9474615|
          39.7505358:-104.9486954|
          39.7505111:-104.950648|
          39.7511215:-104.9506587|
          39.7511173:-104.9527187|
          39.7511091:-104.9546445|
          39.7508987:-104.9565381,
    inner=39.7498:-104.9535|
          39.7498:-104.9525|
          39.7488:-104.9525|
          39.7488:-104.9535|
          39.7498:-104.9535,
    fill=#46FFFF00,
    stroke=#008000,
    width=3.0,
    altMode=clamp_to_ground
""".trimIndent().replace("\n", "")

val scenarios =
    listOf(
        createScenario(
            name = "tower_bridge",
            titleId = R.string.scenarios_tower_bridge,
            initialState =
                "mode=satellite;camera=lat=51.5057832,lng=-0.0751902,alt=5.6035,hdg=-16.36154,tilt=0,range=20000",
            animationString =
                // Wait for the map to be fully loaded and idle before starting the animation.
                "delay=dur=2000;" +
                "waitUntilTheMapIsSteady=timeout=10000;" +
                "flyTo=lat=51.5057832,lng=-0.0751902,alt=5.6035,hdg=-16.36154,tilt=65,range=564,dur=3500;" +
                "delay=dur=1500;" +
                "waitUntilTheMapIsSteady=timeout=10000;" + // Wait up to 3 seconds for the map to steady
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
                "waitUntilTheMapIsSteady;" +
                "delay=dur=2000;" + // Initial 1s delay
                // Fly to a viewpoint near the Empire State Building
                "flyTo=lat=40.748392,lng=-73.986060,alt=174.1,hdg=26.3,tilt=67,range=3977,dur=4500;" +
                "waitUntilTheMapIsSteady=timeout=10000;" + // Wait up to 10 seconds for the map to steady

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
                "waitUntilTheMapIsSteady;" +
                "delay=dur=2000;" + // Initial 1s delay
                // Fly to a viewpoint near the Empire State Building
                "flyTo=lat=40.748392,lng=-73.986060,alt=174.1,hdg=26.3,tilt=67,range=3977,dur=4500;" +
                "waitUntilTheMapIsSteady=timeout=10000;" + // Wait up to 10 seconds for the map to steady

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
                "waitUntilTheMapIsSteady;" +
                "delay=dur=2000", // This scenario runs custom code, not parsed animation string
        ),
        createScenario(
            name = "fly_to",
            titleId = R.string.scenarios_bondi_to_eye,
            initialState =
                "mode=satellite;camera=lat=-33.891984,lng=151.273785,alt=13.3,hdg=274.5,tilt=71,range=3508",
            animationString =
                "delay=dur=2000;" +
                "waitUntilTheMapIsSteady=timeout=10000;" +
                "flyTo=lat=-33.868670,lng=151.204183,alt=39.6,hdg=293.8,tilt=69,range=1512,dur=2500;" +
                "delay=dur=2000",
        ),
        createScenario(
            name = "fly_around",
            titleId = R.string.scenarios_delicate_arch,
            initialState =
                "mode=satellite;camera=lat=36.10145879,lng=-112.10555998,alt=774.39,hdg=33.198,tilt=74.036,range=9180.62",
            animationString =
                "waitUntilTheMapIsSteady=timeout=10000;" +
                "delay=dur=3000;" +
                "flyTo=lat=38.743502,lng=-109.499374,alt=1467,hdg=-10.4,tilt=58.1,range=138.2,dur=3500;" +
                "waitUntilTheMapIsSteady=timeout=10000;" +
                "flyAround=lat=38.743502,lng=-109.499374,alt=1467,hdg=-10.4,tilt=58.1,range=138.2,dur=6000,count=2;" +
                "delay=dur=2000",
        ),
        createScenario(
            name = "markers",
            titleId = R.string.scenarios_markers,
            initialState =
                "mode=satellite;camera=lat=52.51974795,lng=13.40715553,alt=150,hdg=252.7,tilt=79,range=1500",
            animationString =
                "waitUntilTheMapIsSteady;" +
                "delay=dur=2000;" +
                "flyTo=lat=52.522255,lng=13.405010,alt=84.0,hdg=312.8,tilt=66,range=1621,dur=2000;" +
                "delay=dur=3000",
            markers =
                "id=absolute,lat=52.519605780912585,lng=13.406867190588198,alt=150,label= ,altMode=absolute;" +
                "id=relative_to_ground,lat=52.519882191069016,lng=13.407410777254293,alt=50,label= ,altMode=relative_to_ground;" +
                "id=clamp_to_ground,lat=52.52027645136134,lng=13.408271658592406,alt=5,label= ,altMode=clamp_to_ground;" +
                "id=relative_to_mesh,lat=52.520835071144226,lng=13.409426847943774,alt=10,label= ,altMode=relative_to_mesh;"
        ),
        createScenario(
            name = "model",
            titleId = R.string.scenarios_model,
            initialState =
                "mode=satellite;camera=lat=47.133971,lng=11.333161,alt=2200,hdg=221.4,tilt=25,range=30000",
            animationString =
                // Initial delay
                "delay=dur=2000;" +
                "waitUntilTheMapIsSteady=timeout=10000;" +
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
                "delay=dur=2000;" +
                "waitUntilTheMapIsSteady=timeout=10000;" +
                "flyTo=lat=41.901229,lng=-87.621649,alt=179.6,hdg=169.0,tilt=71,range=4145,dur=2500;" +
                "delay=dur=1000",
            polylines =
                "o`v~FnsxuOrAAKy\\S{@cPrJqF`Dc@d@Wb@Uv@It@S~GQ|AYjA]dAo@jAu@z@c@`@}@f@}A`@{PpCkQlCgBJoILW]mDc@o@@wATa@Pg@t@",
        ),
        createScenario(
            name = "polyline-honolulu",
            titleId = R.string.scenarios_polyline_honolulu,
            initialState =
                "mode=satellite;camera=$hawaiiCenter",
            animationString =
                "delay=dur=2000;" +
                "waitUntilTheMapIsSteady=timeout=10000;" +
                "flyAround=$hawaiiCenter,dur=3000,count=0.5;" +
                "flyTo=lat=21.304491,lng=-157.856769,alt=10.4,hdg=117,tilt=58,range=5410,dur=2000;" +
                "delay=dur=500;" +
                "waitUntilTheMapIsSteady=timeout=10000;" +
                "flyTo=lat=21.306388,lng=-157.859271,alt=6.0,hdg=63,tilt=58,range=689,dur=2500;" +
                "delay=dur=750;" +
                "waitUntilTheMapIsSteady=timeout=10000;" +
                "flyTo=lat=21.276715,lng=-157.827153,alt=63.3,hdg=16,tilt=65,range=2121,dur=3500;" +
                "delay=dur=1000;" +
                "waitUntilTheMapIsSteady;" +
                "flyTo=lat=21.262728,lng=-157.808147,alt=89.1,hdg=273,tilt=58,range=5400,dur=3500;" +
                "delay=dur=500;" +
                "waitUntilTheMapIsSteady;" +
                "flyAround=lat=21.262728,lng=-157.808147,alt=89.1,hdg=273,tilt=58,range=5400,dur=5000,count=1.0;" +
                "delay=dur=5000",
            polylines = hawaiiRoute,
            markers =
                "lat=21.306806375289945,lng=-157.85876833645366,alt=30,label=Iolani Palace,altMode=relative_to_mesh;" +
                "lat=21.276300239445394,lng=-157.827331136819,alt=30,label=Waikīkī Beach,altMode=relative_to_ground;" +
                "lat=21.262045979483513,lng=-157.806026546902,alt=30,label=Diamond Head,altMode=relative_to_ground;" +
                ""

        ),
        createScenario(
            name = "polygon-denver",
            titleId = R.string.scenarios_polygon,
            initialState =
                "mode=satellite;camera=lat=39.7498,lng=-104.9535,alt=2000,tilt=60,hdg=200,range=3000",
            animationString =
                "waitUntilTheMapIsSteady=timeout=10000;" +
                "delay=dur=1000;" +
                "flyAround=lat=39.7498,lng=-104.9535,alt=2000,hdg=200,tilt=60,range=3000,dur=3000,count=1.0;" +
                "delay=dur=1000;" +
                "flyTo=lat=39.7498,lng=-104.9535,alt=2000,hdg=200,tilt=60,range=1500,dur=2500;" +
                "delay=dur=1000",
            polygon = denverPolygon,
        )

    ).associateBy { it.name }