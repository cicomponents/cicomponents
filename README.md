CI Components
=============

Most of CI (*continuous integration*) tools are rather difficult to operate. Many of them require a buy-in into their domain model (*what's a project? what's a build step? what's a release?*) and some of them even require an extensive use of UI to configure.

**CI Components** turns the model inside out by letting every project decide what their model and workflow should be like — described in code. To facilitate this, CI Components provides a set of components useful in building a CI setup, as well as a foundation to build more of these components.

CI Components is built upon OSGi so its most friendly to JVM-based projects, but its not at all limited to those.

CI Components can be both run standalone on its own customized [Karaf](http://karaf.apache.org) container as well as deployed onto other, non-customized Karaf containers.

This is a very early version, expect major changes until 1.0 is released.

## Components

This list is intended to grow over time. This is only the beginning.

* `OutputProvider` and `OutputProviderService` to stream and watch timestamped stdour/stderr
* `LatestRevisionGitBranchMonitor` to monitor changes in a git repository
* `PersistentMap` to store simple key/value pairs on disk to remember the state between shutdowns

## Usage

For a brief example, look at [CI Components' own CI definition](https://github.com/cicomponents/cicomponents/tree/master/cicomponents-ci/src/main/java/org/cicomponents/ci)

# Contributing

Contributions of all kinds (code, documentation, testing, artwork, etc.) are highly encouraged. Please open a GitHub issue if you want to suggest an idea or ask a question.

We use Unprotocols [C4 process](http://rfc.unprotocols.org/1/). In a nutshell, this means:

* We merge pull requests rapidly (try!)
* We are open to diverse ideas
* We prefer code now over consensus later
