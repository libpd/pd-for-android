This repository contains a number of sample applications built on top of Pd
for Android:  PdClient is a minimal client; ScenePlayer is an elaborate
client, and CircleOfFifths is a stand-alone activity that uses Pd directly,
without going through a service.

Some general hints regarding quirks of the Android SDK and Eclipse:

 * If you are experiencing problems with Eclipse or the Android Development
   Kit, take a look at this page: http://gitorious.org/pdlib/pages/Eclipse

 * If you want to use ant to build Pd, you first need to say
       android update project --path .
   in each project folder.

