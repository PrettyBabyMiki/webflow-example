# Overview

Spring Web Flow facilitates building web applications that require guided navigation -- e.g. a shopping cart, flight check-in, a loan application, and many others. In contrast to stateless, free-form navigation such use cases have a clear start and end point, one or more screens to go through in a specific order, and a set of changes that are not finalized to the end.

A distinguishing feature is the ability to define a **flow definition** consisting of *states*, *transitions*, and *data*. For example, view states correspond to the individual screens of the flow while transitions are caused by events resulting from the click of a button or a link. Data may be stored in scopes such as *flash*, *view*, *flow*, and others. Scoped data is cleared when it is no longer in scope.

In REST terms a flow represents as a single resource. The same URL used to start the flow is also the URL used to step through the flow (there is also an execution key uniquely identifying the current flow instance). As a result of this approach navigation remains encapsulated in the flow definition.

Some key benefits of using Spring Web Flow:

+ A flow abstraction to model *"long conversations"* in web applications
+ Proper encapsulation for navigation rules
+ Multiple scopes in which to keep data
+ Automatic use of the POST/REDIRECT/GET pattern to avoid browser warnings
+ Impossible to return to completed flow sessions via browser back button
+ Rapid prototyping of flow requirements
+ Development mode in which flow definition changes are detected on the fly
+ IDE visualization for flow definitions
+ Much more...

# Documentation

See the current [Javadoc](http://static.springsource.org/spring-webflow/docs/current/javadoc-api/) and [Reference](http://static.springsource.org/spring-webflow/docs/current/spring-webflow-reference/) docs.

# Samples

The `./spring-webflow-samples` sub-directory contains several samples that can be built with Maven. More samples can be found in the `spring-samples` SVN repository: see [webflow-showcase](https://src.springframework.org/svn/spring-samples/webflow-showcase) and [webflow-primefaces-showcase](https://src.springframework.org/svn/spring-samples/webflow-primefaces-showcase).

# Downloading artifacts

Instructions on [downloading Spring Web Flow](https://github.com/SpringSource/spring-webflow/wiki/Downloading-Spring-Web-Flow-Artifacts) artifacts via Maven and other build systems are available via the project wiki.

# Issue Tracking

Spring Web Flow's JIRA issue tracker can be found [here](http://jira.springsource.org/browse/SWF). If you think you've found a bug, please consider helping to reproduce it by submitting an issue reproduction project via the [spring-webflow-issues](https://github.com/springsource/spring-webflow-issues) repository. The [readme](https://github.com/springsource/spring-webflow-issues#readme) provides simple step-by-step instructions.

# Getting support

Check out the [Spring forums](http://forum.springsource.org/forumdisplay.php?36-Web-Flow) and the [spring-webflow tag](http://stackoverflow.com/questions/tagged/spring-webflow) on StackOverflow. [Commercial support](http://springsource.com/support/springsupport) is available too.

# Building from source

Instructions on [building Spring Web Flow](https://github.com/SpringSource/spring-webflow/wiki/Building-From-Source) from source are available via the project wiki.

# Contributing

[Pull requests](http://help.github.com/send-pull-requests) are welcome. You'll be asked to sign our contributor license agreement ([CLA](https://support.springsource.com/spring_committer_signup)). Trivial changes like typo fixes are especially appreciated (just [fork and edit](https://github.com/blog/844-forking-with-the-edit-button)!). For larger changes, please search through JIRA for similiar issues, creating a new one if necessary, and discuss your ideas with the Spring Web Flow team.

# License

Spring Web Flow is released under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).



