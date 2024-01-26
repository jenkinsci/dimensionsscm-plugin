# Jenkins Dimensions Plugin - User Guide

## Overview

The Jenkins Dimensions Plugin allows a Jenkins job to be associated
with a Dimensions CM stream or project, automatically updating the
Jenkins workspace with file content from the Dimensions CM repository.

The plugin currently supports

-   Polling
-   Checkout
-   Build change log reporting
-   Tagging
-   Artifact Upload
-   Tag Deployment
-   Launching Tagged Builds via Dimensions Builder
-   Credential plugin
-   Pipeline job type

## Pre-requisites

New releases of the plugin are tested with the most recent major version
of Dimensions CM, version 14.x at the time of writing. The plugin should
also work fine with Dimensions CM version 12.x. If you need support for
an older version of Dimensions CM, then older versions of the Jenkins
Dimensions Plugin are available to download and install (for example,
version 0.8.15 should work with Dimensions CM 2009 R1 and 2009 R2).

The plugin uses the Dimensions CM Java client API to communicate with
a specified Dimensions CM server installation and so requires that the
Jenkins installation be updated with a number of JAR files from the
Dimensions CM installation as documented below.

## Installation

To add this plugin to a Jenkins installation, the following steps need
to be taken:

-   The plugin needs to be installed and enabled using the Jenkins
    plugin manager from the *Available* tab in the usual manner. The
    name of the Dimensions CM plugin to select and install is
    *Dimensions Plugin*.
-   The following Dimensions Java client API JAR files need to be added
    to the user class path for the Jenkins JVM, or dropped into the
    *\<JENKINS\_HOME\>*`/plugins/dimensionsscm/WEB-INF/lib` directory.
    These JAR files can be copied from the
    *\<DM\_ROOT\>*`/java_api/lib/` or *\<DM\_ROOT\>*`/AdminConsole/lib/`
    directory of your Dimensions server or client installation
    respectively.
    -   `darius.jar`
    -   `dmclient.jar`
    -   `dmfile.jar`
    -   `dmnet.jar`
-   Depending on the version of the Dimensions Java client API you are
    using, there may be some additional JAR files needed (e.g.
    `commons-logging-api.jar` for CM 12.2 or 14.1;
    and `slf4j-api.jar` for CM 14.3). You should copy these too.

Failure to follow the above steps will mean the plugin will not operate
correctly.

## Configuring the Plugin

The plugin can be configured to work with Dimensions at both the System
level and at the individual job definition level.

### System Configuration

Configuring the plugin at the system level allows you to define a
default Dimensions installation which can be used as the default for
every job. This default installation can be configured by opening the
*Manage Jenkins-\>Configure system* configuration page and looking for
the Dimensions configuration pane shown below.

#### Dimensions Login Details

The standard Dimensions login details need to be provided in the above
fields. This is the Dimensions login details that will be used by Jenkins
to connect to the Dimensions repository and retrieve any updated files. A
*Check Connection...* button is provided for your convenience to ensure
the connection details you have specified are correct and can be used by
Jenkins.

We have several ways of specifying global login details. If you specify
login details in Credentials section you should use credentials plugin
and choose login/password from the list of credentials.

------------------------------------------------------------------------

![global credentials login](/docs/images/global-credentials-login.png)

------------------------------------------------------------------------

In case of specifying login details in *Legacy username/password*
section you should type all credentials manually.

------------------------------------------------------------------------

![global legacy login](/docs/images/global-legacy-login.png)

------------------------------------------------------------------------

Checking the *Use update* toggle will get the plugin to automatically
populate your Jenkins workspace with content from Dimensions. If the
checkbox is not selected, then the plugin will not automatically
populate your workspace.

#### Advanced Options

The *Advanced...* button shows an extra field that allows you to specify
if the Dimensions server installation is running in a different time zone
than the current Jenkins installation. This is useful if you are running
in a geographically distributed environment.

------------------------------------------------------------------------

![global advanced configuration](/docs/images/global-advanced-configuration.png)

------------------------------------------------------------------------

The *Advanced...* button also shows a field that allows you to optionally
specify a Dimensions Web client installation used to access the files and
perform operations on them using the Dimensions Web client.

### Freestyle Job Configuration

When you create a new Jenkins job, you need to configure the Dimensions
stream or project that this job will be monitoring. This can be done
using the standard job *Configure* page.

Using the *Source Code Management* pane, select the Dimensions option
and fill in the details shown below with the Dimensions project that
this job will use.

------------------------------------------------------------------------

![job SCM configuration](/docs/images/job-scm-configuration.png)

------------------------------------------------------------------------

The *Project Name* must refer to the Dimensions project or stream that
this job will monitor. This is a mandatory field.

*Credential type* allows you specify the login details for the
Dimensions server in a number of different ways:
-   *Credentials*: If you use this way of specifying credentials then
    you will be using the Credentials plugin. Just choose login/password
    from the list of credentials.
-   *Global configuration*: In this case Jenkins will use the login
    details specified in the *Configure System* section.
-   *Legacy username/password*: In this case you should specify all
    credentials settings manually.

The *Folder Name* refers to a specific folder name in the Dimensions
project or stream that the job can monitor. This should be specified in
UNIX format and represent the high-level folder from which files will be
monitored. If you leave this field blank or specify '`/`', then all the
contents of the project/stream will be monitored. You can specify
multiple folders to monitor or just leave it blank to monitor
everything.

The *Path Name* refers to a specific folder name in the Dimensions
project or stream that should be excluded from job monitoring.
If you leave this field blank than no folders will be excluded.

A number of options are provided that can be used to control the
behavior of the plugin. These are:

-   Clear the contents of the workspace – checking this option will
    delete the full contents of the workspace before using Dimensions to
    repopulate it. The use of this option is not recommended for very
    large projects as it can significantly increase the build time
-   Always force a build to occur – checking this option will ignore any
    errors or file conflicts reported by the plugin and always force the
    build to be started. If this option is not selected, then any errors
    or conflicts reported by the plugin will automatically abort the
    build.
-   Overwrite any locally modified files in workspace – checking this
    option will automatically overwrite any files that may have been
    modified in the local workspace with files that come from
    Dimensions. The use of this option is not recommended if you are
    using Jenkins to build workspaces that have content that might
    conflict with that stored in the Dimensions repository.

An *Advanced...* tag allows you to override any of the default
Dimensions installation details specified in the system configuration.
The options provided are the same as documented in the *System
Configuration* section above. Options are also provided to control the
permissions on files that are checked out into the Jenkins workspace and
specify if item header substitution is to be used.

### Pipeline Job Configuration

To specify pipeline job with help of Dimensions plugin in
*Pipeline/Workflow* section you can:

1)  Choose *Pipeline/Workflow script* option in the *Definition*
    drop-down list and specify step to checkout from Dimensions CM
    project or stream. This step you can generate with the help of
    the *Pipeline Syntax/Snippet Generator* section, or you can enter
    it manually.
    
    <span style="text-decoration:underline">Syntax:</span>
    
    The name of the command is **dimensionsscm**. Also you should
    specify the required parameters: project name, credentials type
    and login details. Login details can be specified in 3 ways:
    
    1. `credentialsType: 'pluginDefined'`. You need to specify
        the `credentialsId`, `pluginServer`, `pluginDatabase` and `pluginDbConn`
        parameters.
        
        Ex.: `dimensionsscm credentialsType: 'pluginDefined', `
        `credentialsId: 'credentialname', `
        `pluginDatabase: 'cm_typical', `
        `pluginDbConn: 'DIM14', `
        `pluginServer: 'stl-ta-vcw1-9', `
        `project: 'PRODNAME:STREAMNAME'`
        
    2. `credentialsType: 'userDefined'`. You need to specify
        the `userName`, `password`, `userServer`, `userDatabase` and `userDbConn`
        parameters.
        
        Ex.: `dimensionsscm credentialsType: 'userDefined', `
        `userName: 'username', `
        `password: 'P@ssw0rd', `
        `userDatabase: 'cm_typical', `
        `userDbConn: 'DIM14', `
        `userServer: 'stl-ta-vcw12-9', `
        `project: 'PRODNAME:STREAMNAME'`
        
    3.  `credentialsType: 'globalDefined'`. No additional
        parameters are required. In this case all credential
        details will be taken from *Configure System* page.
        
        Ex.: `dimensionsscm credentialsType: 'globalDefined', `
        `project: 'PRODNAME:STREAMNAME'`
        
2)  Choose *Pipeline/Workflow script from SCM* option in the
    *Definition* drop-down list. This means that you can add a file
    with the Pipeline script to your Dimensions stream or project
    and when the job runs, it will be executed from the script in
    the file. Configuration of Pipeline/Workflow job is the same as
    in Freestyle project.

### Job Build Options

Options are also available that allow you to:

-   Lock a project or stream while a build is in progress
-   *Tag* a successful build such that a baseline is automatically
    created in the Dimensions repository on build completion
-   Use a parameterized build to specify a baseline to build, rather
    than a project
-   Use a parameterized build to specify a list of requests to build,
    rather than a project.

These options are described in the following sections below.

#### Locking a Project while a build is running

It is now possible to lock a Dimensions project or stream while a build
is being run, such that no changes maybe made to that project (or
stream) until the build has finished. This option is provided so that
long running builds can be assured that the state of the Dimensions
project that they are building does not change while the build is in
progress. This option should be set if the build process interacts with
Dimensions once the initial checkout is complete and the state of the
project needs to be consistent with the assets being built.

An example of this might be if the build process does a deployment or
release step from Dimensions as part of the build.

This option can be enabled or disabled via the *Lock Dimensions project
while the build is in progress* flag under the **Build Environment**
options.

*(Note* - This option must be set if you intend to tag a successful
build. Failure to do so will automatically fail that build).

#### Tagging a Successful build in Dimensions

It is now possible to *tag* a successful build in Dimensions, such that
a baseline is automatically created to represent the state of the
project or stream that was just built. This option is provided so that
release or checkpoint builds can automatically be tagged in Dimensions
to have an asset that represents that build.

This option can be enabled or disabled via the *Tag successful builds in
Dimensions as a baseline* flag under the **Post-build Actions** options.

An *Advanced...* tag is present that allows you to change the type of
baseline that is created by the tagging process. By default, the tagging
process will create a project baseline, but support is also present for
creating template driven baselines as well. The options that are
currently supported are:

-   Owning baseline part - This is the name of the Dimensions part that
    will own the baseline
-   Baseline template - This is the name of the baseline template which
    will be used to create the baseline. Currently, only item templates
    are supported. Other template types maybe added in a future release.
-   Baseline Scope - This states the type of baseline to be created -
    either a part scoped, project scoped or revised baseline. Currently,
    you should only use baseline templates and owning parts with part
    scoped baselines. Project driven baselines do not support templates
    or owning parts and specifying these options will be ignored by the
    plugin. Revised baselines will only work if you have setup your
    build to be parameterized and provide *DM\_BASELINE* and
    *DM\_TARGET\_REQUEST* as input into the build. *DM\_BASELINE* will
    be used as the source baseline and *DM\_TARGET\_REQUEST* will be
    used as the list of requests that are used to create the new revised
    baseline.
-   Baseline Type - This is the name of the user defined baseline type
    against which the baseline will be created. The default is baseline.

#### Using Parameterized Builds

It is now possible to use a Jenkins build project to build both
baselines and requests using parameters that are provided to each build
when it is being run. This functionality has been added to allow a
common build configuration to be used for repeated release and patch
type builds if necessary, rather than using a named project which may
also contain other unwanted changes. This functionality can be enabled
by adding the following parameters to a Jenkins project using the *This
build is parameterized* option:

-   *DM\_BASELINE* - this string parameter will allow you to specify a
    Dimensions baseline which will be used as the source for the build,
    rather than the usual project. Specifying this option will override
    any project sources that might have been defined, all other options
    however will be honored as per usual. This option should be used if
    you intend to perform a release build from a known baseline of code.

-   *DM\_REQUEST* - this string parameter will allow you to specify a
    list of Dimensions requests (comma separated) which will be used as
    the source for the build, rather than the usual project. Specifying
    this option will override any project or baseline sources that might
    have been defined, all other options however - with the exception of
    folder filters - will be honored as per usual. This option should be
    used if you intend to apply a patch or list of patches to an
    existing area or are building specific controlled features that have
    been controlled by requests. Dependency relationships between
    requests will automatically be processed and any child requests be
    included in the build. When using this option, the Dimensions
    project specified in the job configuration will be used to select
    those *in-response-to* items that are relevant. If a request has
    changed files related to it that are not in the Dimensions project,
    then these changes will be ignored. **Note** - If you are using this
    functionality against a Dimensions 10.1.3 server, then the
    functionality that is offered by this parameter is limited to the
    capabilities present in Dimensions 10.1.3. For example, refactoring
    support is not offered, files will always be checked out as
    read-only and if you specify multiple requests to process, then each
    request will be checked out separately which might cause file
    versions to conflict with each other. If you wish to use this
    functionality, it is strongly suggested that you upgrade to
    Dimensions 2009 R1+.

-   *DM\_TARGET\_REQUEST* - this string parameter will allow you to
    specify a list of Dimensions requests (comma separated) which will
    be used to relate any assets against that get checked into
    Dimensions as a result of a build. This refers primarily to build
    steps that use Dimensions Builder and post build actions such as
    building a tagged baseline and loading assets into Dimensions.

#### Other Job Build Options

This section lists other build options that are available in this
plugin.

##### Deploying Tagged Baselines in Dimensions

It is now possible to automatically deploy a tagged baseline from the
plugin as the last stage of the Jenkins build process. This will
initiate a deployment of the contents of the baseline to all the
deployment nodes associated with a deployment stage and the running of
any deployment pre/post scripts. The plugin does this by running the
Deploy Baseline command (DPB) and returning any results that this
command generates.

This option can be enabled or disabled via the *Automatically deploy the
baseline* flag under the **Post-build Actions** options. This option
will only be presented if the *Tag successful builds in Dimensions as a
baseline* flag is checked. You will also be able to specify the stage
you want the baseline to be deployed to. If you do not specify a stage,
then the next one will be used automatically.

(Note - For the deployment to succeed the project being used as a source
for the build must be configured to allow baseline deployment).

##### Actioning Tagged Baselines in Dimensions

It is now possible to automatically action a tagged baseline from the
plugin as the last stage of the Jenkins build process. This will action
the tagged baseline to a given lifecycle state in Dimensions. The plugin
does this by running the Action Baseline command (ABL) and returning any
results that this command generates.

This option can be enabled or disabled via the *Automatically action the
baseline* flag under the **Post-build Actions** options. This option
will only be presented if the *Tag successful builds in Dimensions as a
baseline* flag is checked. You will also be able to specify the
lifecycle state you want the baseline to be actioned to. If you do not
specify a state, then the next one will be used automatically.

##### Launching Dimension Builder with Tagged Baselines

It is now possible to automatically launch a build in Dimensions Builder
using the tagged baseline as part of the last stage of the Jenkins build
process. This will initiate a baseline build in Dimensions Builder using
build parameters setup in the Jenkins job configuration. The plugin does
this by running the Build Baseline command (BLDB) and returning any
results that this command generates.

This option can be enabled or disabled via the *Automatically build the
baseline* flag under the **Post-build Actions** options. This option
will only be presented if the *Tag successful builds in Dimensions as a
baseline* flag is checked. You are also able to specify:

-   the Dimensions Build area you want to use
-   the Dimensions Build configuration to use
-   the Dimensions Build options
-   the Dimensions Build targets
-   if to run the build in background mode
-   if to perform a clean build
-   if to capture build outputs and build dependencies under Dimensions.

This option should be selected if you want to use Dimensions Builder
within your build process. For example, to perform multi-platform
release builds for the tagged baseline under strict Dimensions control.

##### Saving Build Artifacts into Dimensions

It is now possible to save assets that have been created as a result of
a build process into Dimensions. This option can be enabled or disabled
via the *Load any build artifacts into the Dimensions repository* flag
under the **Post-build Actions** options.

Artifacts which have been identified for loading into Dimensions will
then be put into the project that the plugin is monitoring using DELIVER
or UPLOAD command as appropriate. If you specify files that are already
under control and have not changed, then these files will be ignored. If
you wish to specify a request to save these changes against, then you
should set a project default request using SCWS or use
DM\_TARGET\_REQUEST as commented on above.

You can specify the following advanced options when checking in a file

-   Force files to be checked onto the tip - This will force
    uncontrolled files with the same name as existing Dimensions files
    to be loaded into the repository and placed onto the tip even if
    they are completely unrelated to files already present
-   Force files to be recorded as merged - This will force files which
    might contain revision conflicts to be loaded into the repository
    and recorded as being merged even though no physical code merge has
    taken place
-   Owning part for files - this will put all new files under the
    Dimensions part specified.

This setting can be configured in the **Advanced** tab of the job
configuration.

**Regular Expressions**

Activating this checkbox will give you the opportunity to enter a series
of Java style regular expression patterns that will be used to determine
which files in your workspace you want to consider for saving into
Dimensions. For example, patterns like

-   *.\*\\.obj* - will consider all `.obj` files in any subfolder below
    the workspace
-   *.\*\\.h* - will consider all `.h` files in any subfolder below the
    workspace
-   *src/.\*\\.h* - will consider all `.h` files in the `src/`
    subdirectory below the workspace (this regular expression will only
    match such files on UNIX machines)

All file and subdirectory patterns specified should be made relative to
the workspace root. For example, if your workspace root is
`/usr/jenkins/project/build/` and you want to save files from
`/usr/jenkins/project/build/src/include`, then specify a pattern like
*src/include/.\*\\.h* (this regular expression will only match such
files on UNIX machines).

Regular expressions for uploading artifacts should use a directory
separator character appropriate for the operating system of the machine
where the workspace is located. If the workspace is on a Windows machine
then `\` is the directory separator character (not `/`), but *\\* is
also the escape character in regular expressions, so you will need to
double up the backslash in your regular expression (like *\\\\*) to
match a single `\` directory separator.

Alternatively you can use the sub-pattern *\[/\\\\\]* to match either
the `\` or `/` directory separator character. For example,
*src\[/\\\\\]include\[/\\\\\].\*\\.h* means all `.h` files under the
`src\include\` or `src/include/` directory in the workspace (so will
work on both UNIX and Windows machines); and
*(.+\[/\\\\\])?test\[/\\\\\]helloworld\\.dat* means the file
`helloworld.dat` in a subdirectory named `test` somewhere in the
workspace (on either UNIX or Windows). Regular expression matching is
very powerful, but can also be very complicated to use correctly, so do
find some good
[documentation](http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html)
and maybe an online regular expression testing tool to check that they
actually match the files you expect them to.

**Ant-Style Pattern Matches**

Instead of using regular expression matching, you may prefer to use
Ant-style pattern matches for saving assets to Dimensions. As with the
Java style regular expressions, this option allows you to enter a number
of patterns based on Ant-style pattern matches. Ant-style patterns can
be considerably easier to use correctly than regular expressions (and
handle differences in directory separator characters on different
operating systems for you).

**Inclusion and Exclusion Patterns**

You can specify file exclusions as well as inclusions to apply to the
files selected for upload.

##### Specifying Dimensions Requests to Capture Uploaded Artifacts or Build Assets

If you are loading build artifacts into Dimensions using the *Load any
build artifacts into the Dimensions repository* or *Automatically build
the baseline* options and want to specify Dimensions requests against
which to capture these changes, you can now do so by defining a Jenkins
build parameter called DM\_TARGET\_REQUEST. When you then start a build,
populate this parameter with the comma separated list of requests that
you wish to use and these will be passed on to the appropriate
Dimensions commands.

##### Specifying Permissions of Checked Files

It is possible to specify the permissions of the files which are being
checked out as part of the job configuration. This includes

-   Default permissions - the file's default permissions stored in
    Dimensions
-   Read-only - setting all the files to read only
-   Writeable - setting all the files to writeable.

This setting can be configured in the **Advanced** tab of the job
configuration.

## Distributed Build Support

The plugin supports the distributed build facilities within Jenkins.
There are two main capabilities that the plugin provides which can
potentially be run on a remote node. These are

-   Checking files out of the project/stream being monitored into a
    workspace, and
-   Checking files into the project/stream being monitored from the
    workspace

To use these distributed capabilities, each remote Jenkins node must
have a Dimensions client installation available and in the path. The
remote Jenkins support is provided through *dmcli*, so that remote node
must be a platform against which Dimensions is natively supported. If
you wish to run Jenkins on an unsupported platform - such as Mac OS -
then you can only use that platform as a master node. The master node
support is Java based, so as long as that platform supports Java (and
Jenkins), it should work. However, running the plugin on an unsupported
platform in this way is purely at your own risk. No responsibility is
taken or implied about how the plugin will behave in these conditions.

## Troubleshooting

-   The plugin should work against Dimensions 10 servers, but requires
    Dimensions Java client API JAR files from a Dimensions 2009 R1
    server installation to work correctly. Otherwise you should use
    Dimensions Java client API JAR files from the same version of
    Dimensions as your Dimensions server installation.
-   Upgrading between certain plugin versions may give an error like
    *FATAL: Unable to run checkout callout - null* when running a job.
    If this happens, open the job configuration and save it again to
    resolve the issue.
-   Dimensions operations must be run by a user who has the necessary
    privileges to do that operation. If a user does not have the
    necessary privileges, then the Dimensions operation will fail as one
    would expect.
-   Loading build artifacts into Dimensions will not process controlled
    files which have been deleted, i.e. the deleted files will not be
    removed from the repository.
-   Upgrading to 0.8.5 of the plugin means you will *have to*
    reconfigure any plugin which uses the check-in functionality.
    **Failure to do so will lead to the check-in feature either failing
    or being ignored.**
-   Specifying a reg-ex pattern ".\*" (the default) may not filter out
    all the Dimensions metadata and .dm directories that it should. This
    is a bug which has been around a for a while, but only seems to
    surface occasionally. When using the regex option, it would be best
    to be very specific in the artifacts that you wish to check in.
-   To use Micro Focus SBM SSO to authenticate interactive users logging
-   into Jenkins, protect the Jenkins application in your deployment
    container (e.g. Tomcat) and use Jenkins' *Delegate to servlet
    container* authentication model. Polling and updating by the
    Dimensions Plugin will still use the user account configured as the
    *Login Name* and *Password* in the *Advanced* view of the **Source
    Code Management** section.
-   Jenkins v2.9 - v2.11 were affected by *JENKINS-35906* (causes
    `java.lang.IllegalArgumentException: Failed to instantiate class hudson.scm.SCM`).
    Workaround is to upgrade to Jenkins v2.12 or later.

## Releases

[See the changelog](/CHANGELOG.md)

