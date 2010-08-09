Quick note on building the native pd library, libpdnative.so:

  - The Android NDK docs seem to suggest that the way to build native
    extensions is manually, by running ndk-build.  Once the native libraries
    have been built, you can run ant in order to compile the Java parts and
    build the Android package.  The build.xml that comes with pd-for-android
    follows this approach and makes no attempt to compile native code.

  - For a smoother start with this package, the native library,
    libpdnative.so, is in git, so that you only need to run ndk-build if you
    actually change the native code.

  - If you change the class PdBase.java, you need to run make in the project
    directory in order to update the JNI headers and rebuild libpdnative.so.

