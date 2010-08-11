Some general hints regarding quirks of the Android SDK and Eclipse:

 * Although I am a big fan of the command line and not at all fond of Eclipse,
   I currently find myself unable to support ant builds of
   pd-for-android-samples because the ant tasks of the Android SDK can't
   handle aidl files in library projects.  This doesn't make much sense
   because sharing aidl files between applications seems like the best
   possible use of library projects.  Anyway, the Eclipse plugin can handle
   aidl files, and so I reluctantly recommend Eclipse for working on the Java
   bits of pd-for-android.

 * If you still want to use ant, you need to copy the contents of
   pd-for-android/PdCommon/src to all projects that depend on PdCommon.  After
   that, you can create an ant build file with
   "android update project --path <project_dir>".

 * I experimented with builders for native components in Eclipse, and the
   results have been disappointing.  So, the recommended way to build the
   native components of pd-for-android is make.  Projects with native
   components will have a makefile in their root directory.  (Make sure to
   refresh your projects in Eclipse after building native libraries.)

   Note: The git repositories contain native binaries, and so you only need to
   run make if you change the native components.

 * After importing the projects of pd-for-android into Eclipse, you will
   probably see a number of bogus error messages.  Here's the incantation that
   makes them go away on my machine (your mileage may vary):
      - build the project
      - delete the gen folder
      - build the project again
   Sometimes it takes a few iterations (sometimes cleaning or refreshing the
   project helps, too).  The problem is that Eclipse sometimes complains that
   the gen folder doesn't exist even when it clearly does, and the way to make
   Eclipse aware of the gen folder is to delete the gen folder.  (Seriously.)

Anyway, all this sounds worse than it is.  Once the demons of the initial
import into Eclipse have been exorcized (above), it's usually smooth sailing.
Have fun!
     Peter Brinkmann

