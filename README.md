## How to create an .aar file of pd-for-android

### Using the terminal

1. Clone this repository
2. Go to the repository folder : `cd pd-for-android`
3. run `git submodule update --init --recursive` to initialize and udpate the git submodules
4. Go to the PdCore fodler : `cd PdCore`
5. Assemble the release: `gradle assembleRelease`
6. Now you have your PdCore .aar file in the folder /build/outputs/aar

If you have trouble with your gradle setup or setting your ANDROID_HOME environment variable (step 5), you can alternativly open Android Studio, import a Non-Android Studio project, open the Gradle Toolbar and run the task assembleRelease in the project :PdCore .

Some general hints regarding quirks of the Android SDK and Eclipse:

 * Read the wiki on Eclipse setup:
     https://github.com/libpd/pd-for-android/wiki/eclipse
 * Make sure to use Eclipse 3.7 or later, as well as a recent version of the
   Android SDK and ADT
 * If you want to use ant to build Pd, you first need to say
     android update project --path .
   in each project folder.

