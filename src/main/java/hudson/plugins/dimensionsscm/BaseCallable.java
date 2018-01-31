package hudson.plugins.dimensionsscm;

import jenkins.MasterToSlaveFileCallable;

/**
 * Base class for all Callable tasks in the Jenkins Dimensions Plugin.
 * <p>
 * Currently we are using the SECURITY-144-compat library, which may not be a great idea
 * (see https://github.com/jenkinsci/dimensionsscm-plugin/commit/2c1d115df55cdb73c163853a097ddd2e768e4f47#commitcomment-11685589 ).
 * The parent POM will be updated in next major release to avoid this issue.
 * <p>
 * See also https://wiki.jenkins-ci.org/display/JENKINS/Slave+To+Master+Access+Control.
 */
abstract class BaseCallable extends MasterToSlaveFileCallable<Boolean> {
    /* This abstract class exists temporarily to make it easier to change the base class of all Callable tasks in this plugin. */
}
