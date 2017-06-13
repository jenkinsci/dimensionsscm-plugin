## Purpose of this directory

This `lib` directory contains a `dmclient-stub.jar` stub Java API for Dimensions CM.

The stub API includes interfaces but not implementations for the Java API for Dimensions CM consumed by this Jenkins Dimensions Plugin.

The stub API is _not_ functional, but is provided solely to allow the code in this Git repository to compile, and for no other reason.

The stub API JAR file must _not_ be included in the built HPI/JPI plugin file because it will prevent the real Java API for Dimensions CM from working correctly. This is why the stub API JAR is given as a `system` dependency in `pom.xml`.

The above means that the [**Installation**](https://wiki.jenkins-ci.org/display/JENKINS/Dimensions+Plugin#DimensionsPlugin-Installation) steps on the wiki page still apply. This means copying the named API JAR files from your Dimensions CM Server into the appropriate subdirectory of your Jenkins installation.
