[ ![Download](https://api.bintray.com/packages/pd-for-android/maven/pd-for-android/images/download.svg) ](https://bintray.com/pd-for-android/maven/pd-for-android/_latestVersion)
[![Circle CI](https://circleci.com/gh/libpd/pd-for-android/tree/master.svg?style=shield&circle-token=:circle-token)](https://circleci.com/gh/libpd/pd-for-android/tree/master)
[![Join the chat at https://gitter.im/libpd/pd-for-android](http://img.shields.io/badge/chat-online-brightgreen.svg)](https://gitter.im/libpd/pd-for-android)

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

Add 'pd-core' as a compile dependency to your app:

```gradle
dependencies {
    compile 'org.puredata.android:pd-core:1.0.0-rc2'
    
    // ... other dependencies
}
```


## How to create an .aar file of pd-for-android

### Using the terminal

1. Clone this repository
1. Go to the repository folder: `cd pd-for-android`
1. Initialize and udpate the git submodules: `git submodule update --init --recursive`
1. Assemble the release: `./gradlew PdCore:assembleRelease` (Note: Windows users should run `gradlew`)
1. Now you have your PdCore .aar file in the folder PdCore/build/outputs/aar

Installation of the Android SDK and NDK is required. Define the NDK location by adding a
ndk.dir property to the local.properties file or with the ANDROID_NDK_HOME environment variable.

If you have trouble with your gradle setup or setting your ANDROID_HOME and ANDROID_NDK_HOME
environment variables (step 4), you can alternatively open Android Studio, import as a Gradle
Project, open the Gradle Toolbar and run the task assembleRelease in the project :PdCore.
