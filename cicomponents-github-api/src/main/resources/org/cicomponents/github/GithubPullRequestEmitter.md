# GithubPullRequestEmitter

This is a services that [emits ](../../../../../../../cicomponents-api/src/main/resources/org/cicomponents/ResourceEmitter.md) github pull requests, whenever a new one is opened, reopened or synchronized (pushed to). It will also scan for open, unprocessed pull requests at startup.

Services implementing this interface use the *on-demand targeting* pattern (CI Components will instantiate services with matching properties on demand), so in order to bind them, one must advertise parameters its looking for:

```java
@Reference(target = "(github-repository=cicomponents/cicomponents)")
protected GithubPullRequestEmitter pullRequests;
```

Or, alternatively, using a longer form:

```java
```java
@Reference(target = "(repository=https://github.com/cicomponents/cicomponents)")
protected GithubPullRequestEmitter pullRequests;
```

After it is bound, it should be listened to. Therefore, it is highly recommended that this reference should be static and the listeners can be added in the `@Activate` annotated method.

## GitHubPullRequest

GitHubPullRequest emitted encapsulates the pull request, the reference to the Git repository and the reference to an object that triggered the emission. In fact, it is a superset of [GitRevision](../../../../../../../cicomponents-git-api/src/main/resources/org/cicomponents/git/GitRevisionEmitter.md#GitRevision).
