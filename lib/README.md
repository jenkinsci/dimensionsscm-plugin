## Purpose of this directory

Maven file repo containing the stub Dimensions CM Java API interfaces.

These are a minimal set of interfaces (where each interface has minimal
content, and no implementation) to allow the Jenkins Dimensions Plugin
to compile successfully.

This stub is not intended to be functional, only to allow compilation.

At runtime, the Dimensions CM Java API from a Dimensions CM installation
must be made available in the Jenkins Dimensions Plugin's classpath.

This stub should _not_ be included in the built Jenkins Plugin because
it will prevent the real Dimensions CM Java API (copied from a Dimensions
CM installation) from working correctly.

The [**Installation**](https://wiki.jenkins-ci.org/display/JENKINS/Dimensions+Plugin#DimensionsPlugin-Installation)
steps on the wiki page still apply. You must still copy the API JAR
files from your Dimensions CM Server into the appropriate subdirectory
of your Jenkins installation.
