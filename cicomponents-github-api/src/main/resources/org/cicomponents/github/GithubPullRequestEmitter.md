# GithubPullRequestEmitter

This is a services that [emits ](../../../../../../../cicomponents-api/src/main/resources/org/cicomponents/ResourceEmitter.md) github pull requests, whenever a new one is opened, reopened or synchronized (pushed to). It will also scan for open, unprocessed pull requests at startup.

## GitHubPullRequest

GitHubPullRequest emitted encapsulates the pull request, the reference to the Git repository and the reference to an object that triggered the emission. In fact, it is a superset of [GitRevision](../../../../../../../cicomponents-git-api/src/main/resources/org/cicomponents/git/GitRevisionEmitter.md#GitRevision).
