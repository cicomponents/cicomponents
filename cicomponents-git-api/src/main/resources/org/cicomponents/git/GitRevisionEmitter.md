# GitRevisionEmitter

This is a class of services that [emits ](../../../../../../../cicomponents-api/src/main/resources/org/cicomponents/ResourceEmitter.md) git revisions. The only type available at this moment is `latest` which will emit a git revision of the latest commit on the branch its monitoring, at startup and while continuously monitoring (at 10 seconds interval at the moment).

Services implementing this interface use the *on-demand targeting* pattern (CI Components will instantiate services with matching properties on demand), so in order to bind them, one must advertise parameters its looking for:

```java
@Reference(target = "(&(type=latest)(repository=https://github.com/cicomponents/cicomponents)(branch=master))")
protected GitRevisionEmitter master;
```

After it is bound, it should be listened to. Therefore, it is highly recommended that this reference should be static and the listeners can be added in the `@Activate` annotated method.

## GitRevision <a name="GitRevision"></a>

GitRevision resource encapsulates a reference to the Git repository and the reference to an object that triggered the emission.
