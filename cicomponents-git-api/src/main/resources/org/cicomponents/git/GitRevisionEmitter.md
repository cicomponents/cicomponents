# GitRevisionEmitter

This is a class of services that [emits ](../../../../../../../cicomponents-api/src/main/resources/org/cicomponents/ResourceEmitter.md) git revisions. The only type available at this moment is `latest` which will emit a git revision of the latest commit on the branch its monitoring, at startup and while continuously monitoring (at 10 seconds interval at the moment).

## GitRevision <a name="GitRevision"></a>

GitRevision resource encapsulates a reference to the Git repository and the reference to an object that triggered the emission.
