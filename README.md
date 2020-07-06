[ ![Download](https://api.bintray.com/packages/pd-for-android/maven/pd-for-android/images/download.svg) ](https://bintray.com/pd-for-android/maven/pd-for-android/_latestVersion)
[![Release](https://img.shields.io/github/release/libpd/pd-for-android.svg?label=JitPack)](https://jitpack.io/#libpd/pd-for-android/)
![Build](https://github.com/libpd/pd-for-android/workflows/Android%20CI/badge.svg)

[![Join the chat at https://gitter.im/libpd/pd-for-android](http://img.shields.io/badge/chat-online-brightgreen.svg)](https://gitter.im/libpd/pd-for-android)
 \- Try the chat for problems with setting up the library to work with your app

[![StackOverflow](http://img.shields.io/badge/stackoverflow-libpd-blue.svg)]( http://stackoverflow.com/questions/tagged/libpd )
 \- For questions regarding libpd

## How to use the library

Make sure you have JCenter in your repositories:

```gradle
allprojects {
    repositories {
        jcenter()
        // ... other repositories
    }
}
```

Add the dependency to your app:

```gradle
dependencies {
    implementation 'org.puredata.android:pd-core:1.1.0'
    
    // ... other dependencies
}
```

Please note that pd-for-android depends on the vanilla version of Pure Data. Currently this is Pure Data vanilla version 0.48-0. You can get desktop distributions of it here:
http://msp.ucsd.edu/software.html

If you're building the patch for your app using the extended distribution of Pure Data, or any other distribution that is not vanilla, you should be careful not to use PD objects that are not part of the vanilla distribution, because these will not work with libpd out of the box. It is however possible to add PD externals to your pd-for-android app. For a simple example as to how this could be done see the PdTest app in this repository, specifically [the jni folder](https://github.com/libpd/pd-for-android/tree/master/PdTest/jni) and the [build.gradle](https://github.com/libpd/pd-for-android/tree/master/PdTest/build.gradle) file. If you take this path, you'll need to clone this repository and use it as the base folder for your app, similar to the way described in the following section on creating an .aar file.

## How to create an .aar file of pd-for-android

### Using the terminal

1. Clone this repository
1. Go to the repository folder: `cd pd-for-android`
1. Initialize and udpate the git submodules: `git submodule update --init --recursive`
1. Assemble the release: `./gradlew PdCore:assembleRelease` (Note: Windows users should run `gradlew`)
1. Now you have your PdCore .aar file in the folder PdCore/build/outputs/aar

Installation of the Android SDK and NDK is required. Specify the NDK location by adding an ndk.dir
property to a local.properties file in the pd-for-android root folder, or by defining an ANDROID_NDK_HOME
environment variable.

### Using Android Studio

1. Install Android Studio
1. Make sure the Android SDK and NDK tools are installed and that Android Studio is properly configured to use them
1. Clone and initialize this repository as per steps 1-3 above
1. Create a new Android Studio project by importing `settings.gradle` from the pd-for-android root folder: `File > New > Import Project...`
1. Open the Gradle Toolbar and run the task assembleRelease in the project :PdCore
