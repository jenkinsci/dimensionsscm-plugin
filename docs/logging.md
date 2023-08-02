# Logging to diagnose issues with the plugin

In version 0.8.10 and later of the Jenkins Dimensions Plugin, the plugin uses
JDK logging which can be captured using Jenkins' own *System Log* functionality.

In *Manage Jenkins*, select *System Log*. This will open a list of
*Log Recorders* (might just contain "All Jenkins Logs" if you have not used this
functionality previously).

Add a new *Log Recorder*, you can call it "Dimensions Plugin". You are now going
to add a *Logger* to the *Log Recorder*, in the **Logger** field, type
"`hudson.plugins.dimensionsscm`", and in the **Log level** field, leave the value
as "`ALL`". Save the *Log Recorder* and its contained *Logger*.

When you encounter a repeatable issue, you can navigate to *Manage Jenkins* >
*System Log* > *Dimensions Plugin* and press the **Clear This Log** button.
Then recreate the problem. Having just recreated the issue (and having done as
little else in Jenkins as possible), navigate back to the "Dimensions Plugin"
*Log Recorder*, and it is likely there will then be some useful information to
help understand the cause of the problem that was recreated, so copy-and-paste
this information into the Jira ticket.
