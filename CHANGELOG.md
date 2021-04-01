# Changelog

### 0.8.19

-   _Released: Apr 6, 2021_
-   Bugfix: tables-to-div-regression - Jenkins v2.264+ (so Jenkins LTS
    v2.277+) introduced a new form renderer which broke system and job
    configuration forms if Dimensions plugin was installed
    (JENKINS-65266)
-   Enhancement: The oldest version of Jenkins supported by the
    Dimensions plugin has been moved up to Jenkins LTS v2.222+.
    If you're still using an older version of Jenkins, then there's
    no reason to upgrade to this version of the Dimensions plugin
    yet anyway

### 0.8.18

-   _Released: Feb 25, 2020_
-   Bugfix: Fix for pipelines run on remote agents

### 0.8.17

-   _Released: Jan 17, 2020_
-   Bugfix: Fixed polling mechanism and change log view issues
-   Enhancement: Implemented credential plugin support. Now you can
    choose login/password from the list of credentials
-   Enhancement: Implemented pipeline support for Dimensions SCM
-   Enhancement: Improved the change log view

### 0.8.16

-   _Released: Nov 19, 2018_
-   Bugfix: Baseline names generated using `[CURRENT_DATE]` were no
    longer legal in Dimensions CM 14.4, so changed colon separators to
    period characters. If you rely on the format of the baseline name
    including these (now invalid) colon characters you can revert to the
    old behavior by setting a system property (L3R08939)

### 0.8.15

-   _Released: Sep 13, 2018_
-   Bugfix: Passwords were stored in configuration files with a
    straightforward reversible encoding - it is now encrypted with
    Jenkins' own encryption (SECURITY-1065)
-   Bugfix: The validation on the configuration form was vulnerable to
    CSRF and didn't check Jenkins permissions appropriately
    (SECURITY-1108)
-   *Important Security Note: Upgrading to this version of the plugin,
    in order to get both of these security fixes, is recommended for
    all users*

### 0.8.14

-   _Released: May 3, 2018_
-   Enhancement: Implemented small number of more recent SCM methods to
    improve compatibility with other Jenkins plugins (JENKINS-44583)
-   Enhancement: Added MIT-licensed source for stub version of the Java
    API, so Jenkins.io CI infrastructure can be used (also plugin code
    is now licensed with simple standard MIT license)
-   Bugfix: Post-build baseline operations should change the build state
    (JENKINS-32268)
-   Bugfix: Minor UI changes to make default repository behavior more
    understandable
-   Bugfix: Users without privilege to fetch request detailed
    descriptions can run request-based builds (JENKINS-48645)

### 0.8.13

-   _Released: Jan 26, 2016_
-   Enhancement: Improved diagnostics for missing JARs and for various
    error conditions involving slaves (IOExceptions or inability to
    locate dmcli)

### 0.8.12

-   _Released: May 14, 2015_
-   Bugfix: *Check Connection* button (in Configure pages) could fail to
    connect with a `NullPointerException` in some environments
-   Enhancement: Reduced warnings in the System Log by not calling
    deprecated Jenkins APIs and better diagnostic messages when things
    go wrong
-   Enhancement: Improved handling of non-ASCII filenames in changelogs

### 0.8.11

-   _Released: Mar 25, 2015_
-   Bugfix: Reconfiguring a job to use Ant patterns for the upload
    artifacts post-build action failed in Jenkins 1.596 and later (no
    need for this update unless you have this issue)

### 0.8.10

-   _Released: Mar 23, 2015_
-   Bugfix: Regular expression pattern-matching for upload of build
    artifacts was no longer using the full workspace-relative path, but
    just the filename component of the path; the full workspace-relative
    path is now used again
-   Enhancement: The plugin now uses the Jenkins System log for
    debug-logging, and the logging and build output should include
    better detail about the reasons for any problem than in previous
    releases

### 0.8.9

-   _Released: Dec 10, 2014_
-   Enhancement: Can exclude file path patterns from being monitored for
    changes (for example, useful for built artifacts). Changes to
    excluded file patterns won't trigger a build, but will still be
    shown and updated when a build is eventually started by some
    subsequent change. The patterns to exclude are Ant-style patterns
    using `/` as a separator (even on Windows). For example,
    `**/target/**` would ignore changes in any `target` directory in the
    repository, and `**/*.class` would ignore changes to any `.class`
    files in the repository

### 0.8.8

-   _Released: Oct 29, 2014_
-   Bugfix: Using check out and then check in on projects could lead to
    missed modifications during polling in particular circumstances
-   Bugfix: It was possible for unwanted content to appear in
    config.xml; this should no longer occur

### 0.8.6

-   _Released: Jan 31, 2014_
-   Enhancement: Checking in artifacts now supports exclusion patterns
    as well as inclusion patterns

### 0.8.5

-   _Released: Dec 25, 2013_
-   Enhancement: Checking in artifacts now supports filtering by
    Ant-style patterns as well as regular expressions
-   *Warning: Upgrading to this version means you will have to
    reconfigure any job which uses the check in artifact functionality.
    Failure to do so will end up with the checkin either failing or
    being ignored*

### 0.8.3.1

-   _Released: Nov 25, 2013_
-   Bugfix: Minor bug fixes, Dimensions compatibility enhancements,
    improved multithreading support

### 0.8.1

-   _Released: Mar 9, 2011_
-   Bugfix: Incorporate fix for copying Dimensions plugin information
    around between jobs
-   Enhancement: Support for NOTOUCH option

### 0.7.11

-   _Released: Mar 2, 2011_
-   Bugfix: Compatibility issues with 1.391+.
-   Enhancement: Support for Dimensions 12.1.x.

### 0.7.9

-   _Released: Dec 28, 2010_
-   Enhancement: Add *DM\_BASELINE* to the baseline identifier template
    pattern.
-   Enhancement: Incorporate EOL changes.
-   Enhancement: Polling to support 1.345+.
-   Enhancement: Upgrade Hudson architecture support to latest version.
-   Bugfix/Enhancement: Allow slave processing to also be run on the
    master.
-   Bugfix: Correct command handling on Unix slaves for checkout/checkin
    tasks, e.g. /TMP invalid qualifier error

### 0.7.7

-   _Released: May 22, 2010_
-   Enhancement: Remove *Workspace location* text box. It was not needed
    and just confused issues. This option has been removed from the GUI
    and is now ignored. You can configure a custom workspace location
    using the Hudson **Advanced Project Options**.
-   Enhancement: Using a template pattern to allow the customization of
    the baseline identifier and type that gets created.

### 0.7.6

-   _Released: Apr 30, 2010_
-   Bugfix/Enhancement: Changes to polling support for monitoring
    subdirectories.

### 0.7.5

-   _Released: Apr 26, 2010_
-   Enhancement: Enable no metadata as an option for checkout.

### 0.7.4

-   _Released: Apr 20, 2010_
-   Enhancement: Support for revised baselines using *DM\_BASELINE*
    (source) and *DM\_TARGET\_REQUEST* (revised with)

### 0.7.3

-   _Released: Apr 14, 2010_
-   Enhancement: Enable IHS expansion as an option when checking out.

### 0.7.2

-   _Released: Apr 8, 2010_
-   Bugfix: Request based download did not work with Dimensions 10.x as
    there was a command incompatibility. A different mechanism is now
    used for Dimensions 10.x servers using the FCDI command.
-   Enhancement: Add baseline type parameter for tagging.
-   Bugfix: When create new job, set update to match what is in the
    global config on *Save*.

### 0.7.1 (Feb 28, 2010)

-   Bugfix: Remove project baseline references from some help/error tags
    and refer to baseline instead.
-   Enhancement: Mask command parameters where appropriate for DMCLI
    slave node operations.
-   Enhancement: If project baseline is specified, then ignore invalid
    parameters when executing the command. Avoids unnecessary build
    failures. Updated help to reflect this.

### 0.7.0

-   _Released: Feb 25, 2010_
-   Enhancement: When tagging a project, optionally specify a template,
    part and scope that could be used.

### 0.6.9

-   _Released: Feb 24, 2010_
-   Bugfix: Request based checkouts with multiple directory filters does
    multiple checkouts. It should not as request checkout ignores
    directories.
-   Bugfix: Correct issue with config cache when stream/project ID
    changed
-   Bugfix: Artifact upload had a compatibility issue with Dimensions
    10.x that caused *FATAL: Unable to load build artifacts into
    Dimensions - Invalid attribute: -AdmAttrNames.wset\_is\_stream*
    message.
-   Enhancement: Added support for artifact uploading in distributed
    build environments.

### 0.6.8

-   _Released: Feb 16, 2010_
-   Enhancement: When checking out from a stream/project allow the
    permissions on the file to be set to readonly, writable or default.
-   Enhancement: When checking in from a workspace, allow the checkin to
    be forced if file conflicts are detected.
-   Enhancement: When checking in from a workspace, allow the owning
    part to be specified rather than the defaults set up in the upload
    rules.

### 0.6.7

-   _Released: Feb 12, 2010_
-   Enhancement: Added Dimensions Builder as a valid build step.
-   Enhancement: Be more verbose about what steps the plugin is
    currently doing during checkout process.
-   Enhancement: Add support for distributed build environments -
    checkout operations only
-   Bugfix: The order in the GUI of *Automatically build the baseline*
    and *Automatically deploy the baseline* in the job configuration was
    swapped around. The deploy is done before the build and the order in
    the GUI implied the other order.
-   Bugfix: Resolved a problem with the Dimensions connection getting
    set to `null` when long jobs were running and polls on that same job
    were performed in rapid succession resulting in strange exceptions
    being fired.
-   Enhancement: Generate a set of change logs even when the plugin is
    not being used to update the Hudson workspace.
-   Bugfix: Fixed a spelling mistake with *Files to Montor* in main job
    `config.jelly`.

### 0.6.6

-   _Released: Jan 8, 2010_
-   Enhancement: When Dimensions post-action fails, fail the build.

### 0.6.4

-   _Released: Jan 8, 2010_
-   Bugfix: Fix compatibility issue with Maven Integration Plugin.

### 0.6.3

-   _Released: Jan 3, 2010_
-   Enhancement: Add integration to Dimensions Build to allow the
    launching of a Dimensions build on the tagged baseline.
-   Enhancement: Actioning of baseline in Dimensions.

### 0.6.2

-   _Released: Jan 1, 2010_
-   Enhancement: Deployment of baseline in Dimensions.

### 0.6.1

-   _Released: Dec 30, 2009_
-   Bugfix: Don't overwrite change log for build if multiple project
    directories are being used. Append them, so full change list is
    given for all directories.
-   Enhancement: Give the ability to upload build artifacts into
    Dimensions stream/project based on 1:N regular expression patterns.
-   Bugfix: If plugins attempted to use with non-Dimensions SCM engine,
    then abort.

### 0.6.0

-   _Released: Dec 28, 2009_
-   Enhancement: Add the ability to checkout a given baseline or request
    instead of a stream/project.
-   Bugfix: Report no changes when no changes made in a build rather
    than Failed to determine log.
-   Enhancement: Add the ability to lock a stream/project during the
    build process.
-   Enhancement: Add the ability to tag a successful build.
-   Enhancement: For a given build, enhance the change set processing to
    list the requests that were used to create the new files that went
    into a build.
-   Enhancement: Make the change set reporting use drop down lists
    rather than static lists. Easier to hide unwanted content.
-   Bugfix: Correctly unregister connections from the Dimensions java
    thread pool.
-   Enhancement: Tag all messages from the plugin with the
    \[DIMENSIONS\] prefix.
-   Bugfix: Tidy up output of messages.
-   Enhancement: When performing an initial build for the first time,
    cleanup the target workspace so that only assets from the build are
    present, not other uncontrolled files.
-   Enhancement: Expand on what *Update* means in the boilerplate.
-   *Acknowledgments - many thanks to Keith for all his contributions
    to the above features. His help was much appreciated!*

### 0.5.8

-   _Released: Dec 5, 2009_
-   Enhancement: Add ability to monitor multiple directories in the
    repository.

### 0.5.7

-   _Released: Nov 30, 2009_
-   Bugfix: Fixed broken URL link issue.
-   Enhancement: Removed unnecessary exception stack printing on
    Dimensions errors.

### 0.5.6

-   _Released: Nov 29, 2009_
-   Enhancement: Support added for Dimensions 10.x servers.

### 0.5.4

-   _Released: Nov 25, 2009_
-   Release of the initial version.
