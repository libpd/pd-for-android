## How to create an .aar file of pd-for-android

[![Join the chat at https://gitter.im/libpd/pd-for-android](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/libpd/pd-for-android?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

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

[TBD: Provide hints regarding quirks of the Android SDK, NDK, and Android Studio here.]
