# Jenkins Dimensions Plugin

[![plugin version](https://img.shields.io/jenkins/plugin/v/dimensionsscm.svg?label=plugin&color=blue)](https://plugins.jenkins.io/dimensionsscm)
[![license](https://img.shields.io/badge/license-MIT-blue.svg)](/LICENSE.txt)
[![jenkins version](https://img.shields.io/badge/jenkins-v2.222.4-blue.svg)](https://jenkins.io/download/lts)
[![build status](https://ci.jenkins.io/buildStatus/icon?job=Plugins%2Fdimensionsscm-plugin%2Fmaster&subject=jenkins%20ci)](https://ci.jenkins.io/job/Plugins/job/dimensionsscm-plugin/job/master/)

This plugin integrates the [Dimensions
CM](https://www.microfocus.com/products/dimensions-cm/) SCM with
Jenkins.

It can act as a simple SCM plugin for a stream or project, and
has some support for request or baseline-based builds. It supports
the Credentials plugin for secrets and now works with Jenkins
Pipeline jobs.

It optionally provides a build step to invoke Dimensions CM Build,
an option to lock the project (not stream) for the duration of a
build, and post-build actions to either upload artifacts to the
Dimensions CM repository, or perform simple baseline operations.

After installing the Jenkins Dimensions Plugin within Jenkins,
you MUST follow the following
[installation instructions](/docs/user-guide.md#Installation) to
complete the installation (by copying some JAR files into place)
or the plugin will not work correctly.

A more detailed [user guide](/docs/user-guide.md) is available
with lengthy content from the historical wiki pages for this
plugin.
