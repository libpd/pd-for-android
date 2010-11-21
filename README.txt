Getting all dependencies:

  * After cloning git@gitorious.org:pdlib/pd-for-android.git, cd into the
    pd-for-android directory and say
      git submodules init
      git submodules update
    This step will download the C sources and low-level Java libraries.

Some general hints regarding quirks of the Android SDK and Eclipse:

 * If you are experiencing problems with Eclipse or the Android Development
   Kit, take a look at this page: http://gitorious.org/pdlib/pages/Eclipse

 * If you want to use ant to build Pd, you first need to say
       android update project --path .
   in each project folder.

