# CS528_Activity_Recognizer

Lukatree7, Fang, Shucheng
SharviGhogale, Ghogale, Sharvi
yang-yilu, Yang, Yilu
Sureshvarthya12, Varthya, Suresh

This project used code from: 
* https://developer.android.com/develop/sensors-and-location/sensors/sensors_overview
* https://developer.android.com/develop/sensors-and-location/location/transitions
* https://developer.android.com/develop/sensors-and-location/location/geofencing
* https://developers.google.com/maps/documentation/android-sdk/start?hl=zh-cn
* https://developer.android.com/guide/topics/ui/notifiers/toasts

Special instructions to run our submission: We tried different ways ( Google RA, Type_Step_Detector&Counter, Type_Accelerometer, 4-Steps modification ) to increase the accuracy and reduce the delay in activity recognition. But in the end, there still exist high delay between Still and Walking.

Phone tested on: HUAWEI ANE-AL00 Android 9 Smartphone ( Deployed with Gspace for Google play service )
Computer Tested on: Lenovo Legion, 3.8 GHz CPU, 16GB RAM

Tips: You need to connect WIFI to access Google Map Service.

Before running the project, make sure you have the file "local.properties" under the project_3 folder.

Inside it, there should be 2 lines of code:

* sdk.dir=xxx\\Android\\Sdk

* MAPS_API_KEY=xxx

Replace the "sdk.dir" with ur local android sdk path.
