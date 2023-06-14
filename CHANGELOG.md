# Changelog

## 0.9.3.1
📅 released: 2023-06-14.
### 🐛 Bug fixes
* [SECURITY-3138](https://www.jenkins.io/security/advisories/) -
  Missing permission check allows enumerating credentials IDs
* [SECURITY-1108](https://www.jenkins.io/security/advisories/) -
  Potential exposure of system-scoped credentials
* *Important Security Note: Upgrading to this version of the plugin, in order
  to get both of these security fixes, is recommended for all users*
## ✍ Other changes
* Merge back 0.9.3.x into default branch (#98) @daniel-beck
* Applied then reverted "Forward compatibility with jenkins-buttons" (#82, #88) @NotMyFault
* Replace git.io with expanded URLs (#61) @MarkEWaite
* Create jenkins-security-scan.yml @dconneely
* EOL JSR 305 (#43) @basil
## 📦 Dependency updates
* Minor version updates to GitHub Action workflows
  * Bump release-drafter/release-drafter from 5.15.0 to 5.21.1 (#51, #62, #79) @dependabot
  * Bump github/codeql-action from 1 to 2 (#60) @dependabot
  * Bump actions/cache from 2.1.7 to 3.0.11 (#58, #64, #80) @dependabot
  * Bump actions/setup-java from 2 to 3 (#59) @dependabot
  * Bump actions/checkout from 2 to 3 (#52) @dependabot
* Bump plugin from 4.31 to 4.32 (#42) @dependabot

## 0.9.3
📅 released: 2021-12-02.
### 🐛 Bug fixes
* For Jenkins 2.297+, fixed issue with NoClassDefFoundError for Commons
  Digester. Unless you are affected by this (which only occurs with very recent
  versions of Jenkins core) there is no need to update to this plugin version
### 📦 Dependency updates
* Bump plugin from 4.24 to 4.31
* Bump actions/cache from 2.1.6 to 2.1.7

## 0.9.2
📅 released: 2021-08-22.
### 🚀 New features and improvements
* [JENKINS-66107](https://issues.jenkins.io/browse/JENKINS-66107) -
  Fixed broken config forms if the Dimensions CM Java API is not found
* [JENKINS-66107](https://issues.jenkins.io/browse/JENKINS-66107) -
  Implement a warning monitor if the Dimensions CM Java API is not found
### 📦 Dependency updates
* Bump plugin from 4.18 to 4.24 (#32, #34) @dependabot
* Bump actions/cache from 2.1.5 to 2.1.6 (#28) @dependabot
* Bump bom-2.222.x from 29 to 887.vae9c8ac09ff7 (#25, #31) @dependabot

## 0.9.1
📅 released: 2021-05-12.
### 📦 Dependency updates
* Bump actions/cache from v2.1.4 to v2.1.5 (#22) @dependabot
* Bump plugin from 4.17 to 4.18 (#20) @dependabot
* Bump bom-2.222.x from 27 to 29 (#19, #24) @dependabot
### 👻 Maintenance
* [JENKINS-65161](https://issues.jenkins.io/browse/JENKINS-65161) - remove
  digester 2 from core (#21) @olamy
* Make digester 3 work correctly

## 0.9.0
📅 released: 2021-04-12.
### 🚀 New features and improvements
* File paths are now shown sorted within each changeset
### 🐛 Bug fixes
* tables-to-div-regression in changes page. The chnages page now works more
  like for other SCMs (without 'Files...' buttons) and is laid out as designed
  and expected in both old and new versions
### 📦 Dependency updates
* Bump actions/setup-java from v1 to v2 (#18) @dependabot
* Bump actions/cache from v2 to v2.1.4 (#17) @dependabot
* Bump bom-2.222.x from 26 to 27 (#16) @dependabot

## 0.8.19
📅 released: 2021-04-06.
### 🐛 Bug fixes
* [JENKINS-65266](https://issues.jenkins.io/browse/JENKINS-65266) -
  tables-to-div-regression in Jenkins v2.264+ (so Jenkins LTS v2.277+)
  introduced a new form renderer which broke system and job configuration
  forms if Jenkins Dimensions Plugin was installed. There remains a minor
  layout issue with the Changes page for a job
* Remove unneeded configuration in buildPlugin (#13) @timja
* Fix for mutual authentication auth (#12) @AnastasiaKozachuk
### 📦 Dependency updates
* The oldest version of Jenkins core supported by the Dimensions plugin has
  been moved up to Jenkins LTS v2.222+. If you're still using an older version
  of Jenkins core, then there is no reason to upgrade to this version of the
  Jenkins Dimensions Plugin yet anyway

## 0.8.18
📅 released: 2020-02-25.
### 🐛 Bug fixes
* Fix for jenkins pipeline scripts and jenkins pipeline called on remote (#11)
  @AnastasiaKozachuk

## 0.8.17
📅 released: 2020-01-17.
### 🚀 New features and improvements
* Implemented credential plugin support. Now you can choose login/password
  from the list of credentials
* Implement mutual authentication (#9) @AnastasiaKozachuk
* Implement pipeline support for Dimensions SCM (#8, #10) @AnastasiaKozachuk
* Improved the change log view
### 🐛 Bug fixes
* Fix change log view issues
* Fix polling mechanism (#7) @AnastasiaKozachuk
* IDE cleanup, better use of generics, better doc in pom (#6) @dconneely

## 0.8.16
📅 released: 2018-11-19.
### 🐛 Bug fixes
* Baseline names generated using `[CURRENT_DATE]` were no longer legal in
  Dimensions CM 14.4, so changed colon separators to period characters. If you
  rely on the format of the baseline name including these (now invalid) colon
  characters you can revert to the old behavior by setting a system property
  (L3R08939)

## 0.8.15
📅 released: 2018-09-13.
### 🐛 Bug fixes
* [SECURITY-1065](https://www.jenkins.io/security/advisories/) - Passwords
  were stored in configuration files with a straightforward reversible
  encoding - it is now encrypted with Jenkins' own encryption
* [SECURITY-1108](https://www.jenkins.io/security/advisories/) - The
  validation on the configuration form was vulnerable to CSRF and didn't check
  Jenkins permissions appropriately
* *Important Security Note: Upgrading to this version of the plugin, in order
  to get both of these security fixes, is recommended for all users*

## 0.8.14
📅 released: 2018-05-03.
### 🚀 New features and improvements
* [JENKINS-44583](https://issues.jenkins.io/browse/JENKINS-44583) -
  Implemented small number of more recent SCM methods to improve compatibility
  with other Jenkins plugins
* Added MIT-licensed source for stub version of the Java API, so Jenkins.io CI
  infrastructure can be used (also plugin code is now licensed with simple
  standard MIT license)
### 🐛 Bug fixes
* [JENKINS-32268](https://issues.jenkins.io/browse/JENKINS-32268) - Post-build
  baseline operations should change the build state
* Minor UI changes to make default repository behavior more understandable
* Changed help tip for CM server field (#5) @haluz
* [JENKINS-48645](https://issues.jenkins.io/browse/JENKINS-48645) - Users
  without privilege to fetch request detailed descriptions can run
  request-based builds

## 0.8.13
📅 released: 2016-01-26.
### 🚀 New features and improvements
* Improved diagnostics for missing JARs and for various error conditions
  involving slaves (IOExceptions or inability to locate dmcli)

## 0.8.12
📅 released: 2015-05-14.
### 🚀 New features and improvements
* Reduced warnings in the System Log by not calling deprecated Jenkins APIs
  and better diagnostic messages when things go wrong
* Improved handling of non-ASCII filenames in changelogs
### 🐛 Bug fixes
* *Check Connection* button (in Configure pages) could fail to connect with a
  `NullPointerException` in some environments

## 0.8.11
📅 released: 2015-03-25.
### 🐛 Bug fixes
* Reconfiguring a job to use Ant patterns for the upload artifacts post-build
  action failed in Jenkins 1.596 and later (no need for this update unless you
  have this issue)

## 0.8.10
📅 released: 2015-03-23.
### 🚀 New features and improvements
* The plugin now uses the Jenkins System log for debug-logging, and the
  logging and build output should include better detail about the reasons for
  any problem than in previous releases
### 🐛 Bug fixes
* Regular expression pattern-matching for upload of build artifacts was no
  longer using the full workspace-relative path, but just the filename
  component of the path; the full workspace-relative path is now used again

## 0.8.9
📅 released: 2014-12-10.
### 🚀 New features and improvements
* Can exclude file path patterns from being monitored for changes (for
  example, useful for built artifacts). Changes to excluded file patterns
  won't trigger a build, but will still be shown and updated when a build is
  eventually started by some subsequent change. The patterns to exclude are
  Ant-style patterns using `/` as a separator (even on Windows). For example,
  `**/target/**` would ignore changes in any `target` directory in the
  repository, and `**/*.class` would ignore changes to any `.class` files in
  the repository
* Exclusion filters added to exclude paths from monitoring when polling SC...
  (#4) @bkyrylo

## 0.8.8
📅 released: 2014-10-29.
### 🐛 Bug fixes
* Using check out and then check in on projects could lead to missed
  modifications during polling in particular circumstances
* It was possible for unwanted content to appear in config.xml; this should no
  longer occur
* Dimensions Projects(Baselines, Requests) filtering only by LAST_UPDATED_...
  (#3) @bkyrylo
* Allow use of variables in the project name, and the use of project versions
  (#2) @dconneely

## 0.8.6
📅 released: 2014-01-31.
### 🚀 New features and improvements
* Checking in artifacts now supports exclusion patterns as well as inclusion
  patterns

## 0.8.5
📅 released: 2013-12-25.
### 🚀 New features and improvements
* Checking in artifacts now supports filtering by Ant-style patterns as well
  as regular expressions
* *Warning: Upgrading to this version means you will have to reconfigure any
  job which uses the check in artifact functionality. Failure to do so will
  end up with the checkin either failing or being ignored*

## 0.8.3.1
📅 released: 2013-11-25.
### 🐛 Bug fixes
* Minor bug fixes, Dimensions compatibility enhancements, improved
  multithreading support
* Update Dimensions SCM to use lastBuild vs. lastSuccessfulBuild for checkout
  (#1) @jmcgarr

## 0.8.1
📅 released: 2011-03-09.
### 🚀 New features and improvements
* Support for NOTOUCH option
### 🐛 Bug fixes
* Incorporate fix for copying Dimensions plugin information around between
  jobs

## 0.7.11
📅 released: 2011-03-02.
### 🚀 New features and improvements
* Support for Dimensions 12.1.x
### 🐛 Bug fixes
* Compatibility issues with Jenkins v1.391+

## 0.7.9
📅 released: 2010-12-28.
### 🚀 New features and improvements
* Add *DM\_BASELINE* to the baseline identifier template pattern
* Incorporate EOL changes
* Polling to support Jenkins v1.345+
* Allow agent processing to also be run on the master
### 🐛 Bug fixes
* Correct command handling on Unix agents for checkout/checkin tasks, e.g.
  /TMP invalid qualifier error
### 📦 Dependency updates
* Upgrade Hudson architecture support to latest version

## 0.7.7
📅 released: 2010-05-22.
### 🚀 New features and improvements
* Remove *Workspace location* text box. It was not needed and just confused
  issues. This option has been removed from the GUI and is now ignored. You
  can configure a custom workspace location using the Hudson
  **Advanced Project Options**
* Using a template pattern to allow the customization of the baseline
  identifier and type that gets created

## 0.7.6
📅 released: 2010-04-30.
### 🚀 New features and improvements
* Changes to polling support for monitoring subdirectories

## 0.7.5
📅 released: 2010-04-26.
### 🚀 New features and improvements
* Enable NOMETADATA as an option for checkout

## 0.7.4
📅 released: 2010-04-20.
### 🚀 New features and improvements
* Support for revised baselines using *DM\_BASELINE* (source) and
  *DM\_TARGET\_REQUEST* (revised with)

## 0.7.3
📅 released: 2010-04-14.
### 🚀 New features and improvements
* Enable IHS expansion as an option when checking out

## 0.7.2
📅 released: 2010-04-08.
### 🚀 New features and improvements
* Add baseline type parameter for tagging
### 🐛 Bug fixes
* Request based download did not work with Dimensions 10.x as there was a
  command incompatibility. A different mechanism is now used for Dimensions
  10.x servers using the FCDI command
* When create new job, set update to match what is in the global config on
  *Save*

## 0.7.1
📅 released: 2010-02-28.
### 🚀 New features and improvements
* Mask command parameters where appropriate for DMCLI slave node operations
* If project baseline is specified, then ignore invalid parameters when
  executing the command. Avoids unnecessary build failures. Updated help to
  reflect this
### 🐛 Bug fixes
* Remove project baseline references from some help/error tags and refer to
  baseline instead

## 0.7.0
📅 released: 2010-02-25.
### 🚀 New features and improvements
* When tagging a project, optionally specify a template, part and scope that
  could be used

## 0.6.9
📅 released: 2010-02-24.
### 🚀 New features and improvements
* Added support for artifact uploading in distributed build environments
### 🐛 Bug fixes
* Request based checkouts with multiple directory filters does multiple
  checkouts. It should not as request checkout ignores directories
* Correct issue with config cache when stream/project ID changed
* Artifact upload had a compatibility issue with Dimensions 10.x that caused
  *FATAL: Unable to load build artifacts into Dimensions - Invalid attribute:
  -AdmAttrNames.wset\_is\_stream* message

## 0.6.8
📅 released: 2010-02-16.
### 🚀 New features and improvements
* When checking out from a stream/project allow the permissions on the file to
  be set to readonly, writable or default
* When checking in from a workspace, allow the checkin to be forced if file
  conflicts are detected
* When checking in from a workspace, allow the owning part to be specified
  rather than the defaults set up in the upload rules

## 0.6.7
📅 released: 2010-02-12.
### 🚀 New features and improvements
* Added Dimensions Builder as a valid build step
* Be more verbose about what steps the plugin is currently doing during
  checkout process
* Add support for distributed build environments - checkout operations only
* Generate a set of change logs even when the plugin is not being used to
  update the Hudson workspace
### 🐛 Bug fixes
* The order in the GUI of *Automatically build the baseline* and
  *Automatically deploy the baseline* in the job configuration was swapped
  around. The deploy is done before the build and the order in the GUI implied
  the other order
* Resolved a problem with the Dimensions connection getting set to `null` when
  long jobs were running and polls on that same job were performed in rapid
  succession resulting in strange exceptions being thrown
* Fixed a spelling mistake with *Files to Montor* in main job `config.jelly`

## 0.6.6
📅 released: 2010-01-08.
### 🚀 New features and improvements
* When Dimensions post-action fails, fail the build

## 0.6.4
📅 released: 2010-01-08.
### 🐛 Bug fixes
* Fix compatibility issue with Maven Integration Plugin

## 0.6.3
📅 released: 2010-01-03.
### 🚀 New features and improvements
* Add integration to Dimensions Build to allow the launching of a Dimensions
  build on the tagged baseline
* Actioning of baseline in Dimensions

## 0.6.2
📅 released: 2010-01-01.
### 🚀 New features and improvements
* Deployment of baseline in Dimensions

## 0.6.1
📅 released: 2009-12-30.
### 🚀 New features and improvements
* Give the ability to upload build artifacts into Dimensions stream/project
  based on 1:N regular expression patterns
### 🐛 Bug fixes
* Don't overwrite change log for build if multiple project directories are
  being used. Append them, so full change list is given for all directories
* If plugins attempted to use with non-Dimensions SCM engine, then abort

## 0.6.0
📅 released: 2009-12-28.
### 🚀 New features and improvements
* Add the ability to checkout a given baseline or request instead of a
  stream/project
* Add the ability to lock a stream/project during the build process
* Add the ability to tag a successful build
* For a given build, enhance the change set processing to list the requests
  that were used to create the new files that went into a build
* Make the change set reporting use drop down lists rather than static lists.
  Easier to hide unwanted content
* Tag all messages from the plugin with the \[DIMENSIONS\] prefix
* When performing an initial build for the first time, cleanup the target
  workspace so that only assets from the build are present, not other
  uncontrolled files
* Expand on what *Update* means in the boilerplate
### 🐛 Bug fixes
* Report no changes when no changes made in a build rather than Failed to
  determine log
* Tidy up output of messages
* Correctly unregister connections from the Dimensions java thread pool
* *Acknowledgments - many thanks to Keith for all his contributions
  to the above features. His help was much appreciated!*

## 0.5.8
📅 released: 2009-12-05.
### 🚀 New features and improvements
* Add ability to monitor multiple directories in the repository

## 0.5.7
📅 released: 2009-11-30.
### 🚀 New features and improvements
* Removed unnecessary exception stack printing on Dimensions errors
### 🐛 Bug fixes
* Fixed broken URL link issue

## 0.5.6
📅 released: 2009-11-29.
### 🚀 New features and improvements
* Support added for Dimensions 10.x servers

## 0.5.4
📅 released: 2009-11-25.
### 🚀 New features and improvements
* Release of the initial version
