## How to create an .aar file of pd-for-android

### Using the terminal

1. Clone this repository
2. Go to the repository folder : `cd pd-for-android`
3. run `git submodule update --init --recursive` to initialize and udpate the git submodules
4. Go to the PdCore fodler : `cd PdCore`
5. Assemble the release: `gradle assembleRelease`
6. Now you have your PdCore .aar file in the folder /build/outputs/aar

Installation of the Android SDK and NDK is required. Define the NDK location by adding a
ndk.dir property to the local.properties file or with the ANDROID_NDK_HOME environment variable.

If you have trouble with your gradle setup or setting your ANDROID_HOME and ANDROID_NDK_HOME
environment variables (step 5), you can alternatively open Android Studio, import a Non-Android
Studio project, open the Gradle Toolbar and run the task assembleRelease in the project :PdCore.

[TBD: Provide hints regarding quirks of the Android SDK, NDK, and Android Studio here.]
