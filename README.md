# Excuser

![Showcase](https://github.com/squareboat/Excuser/blob/master/art/feature_graphics.png)

Excuser is a utility app to rescue yourself from an awkward situation, boring meetings, annoying conversation, meaningless interviews etc.

Just shake your wrist with Andorid Wear and the fake call will ring your phone instantly.

The app allows you to:

* Specify the intensity of the shake which triggers the incoming call.
* Specify custom contacts (which are chosen randomly) for the incoming call.

**Escape boredom. No need to make boring excuses - Just get Excuser!**

### Specs

[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://github.com/squareboat/Excuser/blob/master/LICENSE)

### Download

[![Excuser on Google Play](https://github.com/squareboat/Excuser/blob/master/art/google_play.png)](https://play.google.com/store/apps/details?id=com.squareboat.excuser)


### Compiling Excuser
Use Android Studio and compile it with Gradle. 

To compile it:

* Check out the project with a `git clone <clone URL>`
* Import it to Android Studio by going to File -> Import Project... then selecting the build.gradle file in the root of the project

##### Providing a Fabric Credentials
For release builds, copy the `mobile/fabric.properties.sample` to `mobile/fabric.properties` and add in the keys in it. I have made the configuration such that fabric will only work in release builds.

##### Providing a Signing Configuration
For release builds, copy the `keystore.properties.sample` to `keystore.properties` and add in the keys in it.


## License


    Copyright 2017 SquareBoat

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
