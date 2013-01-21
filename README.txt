Getting all dependencies:

  * First, clone Pd for Android:
      git clone git://github.com/libpd/pd-for-android.git
    Then, install the libpd and MIDI submodules:
      cd pd-for-android
      git submodule init
      git submodule update
    This step will download the C sources and low-level Java libraries.

Some general hints regarding quirks of the Android SDK and Eclipse:

 * Read the wiki on Eclipse setup:
     https://github.com/libpd/pd-for-android/wiki/eclipse
 * Make sure to use Eclipse 3.7 or later, as well as a recent version of the
   Android SDK and ADT
 * If you want to use ant to build Pd, you first need to say
     android update project --path .
   in each project folder.

