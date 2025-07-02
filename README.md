[![Build](https://github.com/googlemaps-samples/android-maps3d-samples/actions/workflows/build.yml/badge.svg)](https://github.com/googlemaps-samples/android-maps3d-samples/actions/workflows/build.yml)

![Contributors](https://img.shields.io/github/contributors/googlemaps-samples/android-maps3d-samples?color=green)
[![License](https://img.shields.io/github/license/googlemaps-samples/android-maps3d-samples?color=blue)][license]
[![StackOverflow](https://img.shields.io/stackexchange/stackoverflow/t/google-maps?color=orange&label=google-maps&logo=stackoverflow)](https://stackoverflow.com/questions/tagged/android+google-maps)
[![Discord](https://img.shields.io/discord/676948200904589322?color=6A7EC2&logo=discord&logoColor=ffffff)][Discord server]

# Google Maps 3D SDK for Android Samples


| Hello Map                                                                            | Camera                                                                                            | Markers                                   | Polygons                                  | Polylines                                 | Models                                  |
|:-------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------| :---------------------------------------- | :---------------------------------------- | :---------------------------------------- | :---------------------------------------- |
| <img src="images/HelloMapActivity_framed.png" alt="Hello Map Activity" width="121"/> | <img src="images/CameraControlsActivity_framed.png" alt="Camera Controls Activity" width="121"/> | <img src="images/MarkersActivity_framed.png" alt="Markers Activity" width="121"/> | <img src="images/PolygonsActivity_framed.png" alt="Polygons Activity" width="121"/> | <img src="images/PolylinesActivity_framed.png" alt="Polylines Activity" width="121"/> | <img src="images/ModelsActivity_framed.png" alt="Models Activity" width="121"/> |

## Description

The [Google Maps Platform Maps3D SDK for Android](https://developers.google.com/maps/documentation/maps-3d/android-sdk) (Experimental Preview) 
empowers developers to bring immersive, real-world 3D experiences into their Android applications. This SDK allows you to render detailed 3D buildings, 
terrain, and landmarks, offering users a richer and more interactive way to explore geographical data. Key features include programmatic camera control 
for cinematic fly-throughs, the ability to add 3D markers and models to the map, and drawing custom polylines and polygons in a 3D space.

This repository provides a collection of sample applications to help you get started and explore the capabilities of the Maps3D SDK:

* **[Maps3DSamples/ApiDemos](Maps3DSamples/ApiDemos)**: This module showcases the fundamental API features using Kotlin and traditional
Android Views. It's a great starting point to understand core concepts like map instantiation, camera manipulation, and adding various map objects.
* **[Maps3DSamples/advanced](Maps3DSamples/advanced)**: For developers using Jetpack Compose, this sample demonstrates
how to integrate the Maps3D SDK into a declarative UI. It utilizes helper classes to bridge the SDK's View-based SDK with Compose, offering a more modern
approach to building UIs with 3D maps.

## View-based Kotlin Samples

**[Maps3DSamples/ApiDemos]([Maps3DSamples/ApiDemos])**: This repo contains the following samples:

**[Maps3DSamples/ApiDemos/kotlin-app](Maps3DSamples/ApiDemos/kotlin-app)**: Demonstrates various features of the **Maps3D SDK for Android**
using Kotlin and Android Views. This module includes examples for:

* Basic map setup ("Hello Map")
* Camera controls and restrictions
* Adding and customizing markers
* Drawing polygons and polylines
* Loading and displaying 3D models

The [Maps3DSamples/ApiDemos/common](Maps3DSamples/ApiDemos/common) module contains shared utilities and layouts.

## Jetpack Compose Samples

**[Maps3DSamples/advanced](Maps3DSamples/advanced)** demonstrates options for integrating the Maps3D SDK into a Jetpack Compose UI.
It also has [Map3dViewModel](Maps3DSamples/advanced/app/src/main/java/com/example/advancedmaps3dsamples/common/Map3dViewModel.kt) which demonstrates how to
create an abstract base class for view models needing to interact with the Maps3DView via a GoogleMap3D.

## Requirements

To run the samples, you will need:

- To [sign up with Google Maps Platform] and enable **Maps3D API for Android**. 
- An [API key] associated with the project above. Follow the [API key instructions] if you're new to the process.
- Copy `Maps3DSamples/ApiDemos/local.defaults.properties` to `Maps3DSamples/ApiDemos/secrets.properties` and set the value of `MAPS3D_API_KEY` to your API key.
- (for the advanced sample, copy `Maps3DSamples/advanced/local.defaults.properties` to `Maps3DSamples/advanced/secrets.properties` and set the value of `MAPS3D_API_KEY` to your API key.)
- All samples require up-to-date versions of the Android build tools and the Android support repository.

## Running the sample(s)

1.  Download the samples by cloning this repository:
    ```bash
    git clone https://github.com/googlemaps-samples/android-maps3d-samples.git
    ```
2.  In the welcome screen of Android Studio, select "Open".
3.  Navigate to the root directory of this cloned repository and select it, or navigate into a specific sample directory, open either of the base projects,`Maps3DSamples/ApiDemos` or `Maps3DSamples/advanced`.

Alternatively, use the `gradlew build` command within a sample's directory to build the project directly or download an APK
under [releases](https://github.com/googlemaps/android-maps3d-samples/releases) (if available for the specific sample).

## Contributing

Contributions are welcome and encouraged! If you'd like to contribute, send us a [pull request] and refer to our [code of conduct] and [contributing guide].

## Terms of Service

This sample uses Google Maps Platform services. Use of Google Maps Platform services through this sample is subject to the Google Maps Platform [Terms of Service].

If your billing address is in the European Economic Area, effective on 8 July 2025, the [Google Maps Platform EEA Terms of Service](https://cloud.google.com/terms/maps-platform/eea) will apply to your use of the Services. Functionality varies by region. [Learn more](https://developers.google.com/maps/comms/eea/faq).

This sample is not a Google Maps Platform Core Service. Therefore, the Google Maps Platform Terms of Service (e.g. Technical Support Services, Service Level Agreements, and Deprecation Policy) do not apply to the code in this sample.

## Support

This sample is offered via an open source [license]. It is not governed by the Google Maps Platform Support [Technical Support Services Guidelines], the [SLA], or the [Deprecation Policy]. However, any Google Maps Platform services used by the sample remain subject to the Google Maps Platform Terms of Service.

If you find a bug, or have a feature request, please [file an issue] on GitHub. If you would like to get answers to technical questions from other Google Maps Platform developers, ask through one of our [developer community channels]. If you'd like to contribute, please check the [contributing guide].

You can also discuss this sample on our [Discord server].

[android-sdk]: https://developers.google.com/maps/documentation/android-sdk
[API key]: https://developers.google.com/maps/documentation/android-sdk/get-api-key
[API key instructions]: https://developers.google.com/maps/documentation/android-sdk/config#step_3_add_your_api_key_to_the_project

[code of conduct]: ?tab=coc-ov-file#readme
[contributing guide]: CONTRIBUTING.md
[Deprecation Policy]: https://cloud.google.com/maps-platform/terms
[developer community channels]: https://developers.google.com/maps/developer-community
[Discord server]: https://discord.gg/hYsWbmk
[file an issue]: https://github.com/googlemaps-samples/android-maps3d-samples/issues/new/choose
[license]: LICENSE
[pull request]: https://github.com/googlemaps-samples/android-maps3d-samples/compare
[project]: https://developers.google.com/maps/documentation/android-sdk/cloud-setup#enabling-apis
[Sign up with Google Maps Platform]: https://console.cloud.google.com/google/maps-apis/start
[SLA]: https://cloud.google.com/maps-platform/terms/sla
[Technical Support Services Guidelines]: https://cloud.google.com/maps-platform/terms/tssg
[Terms of Service]: https://cloud.google.com/maps-platform/terms

