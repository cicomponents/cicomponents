# OutputProviderService

This service is the entry point to collecting stdout/stderr streams. It can be bound using the following declaration:

```java
@Reference
protected volatile OutputProviderService outputProviderService;
```

Now, we can create an `OutputProvider` and stream to it:

```java
OutputProvider outputProvider = outputProviderService.createOutputProvider();
OutputStream standardOutput = outputProvider.getStandardOutput();
OutputStream standardError = outputProvider.getStandardError();

standardOutput.write("Hello, world!\n".getBytes());
```

This is a perfect tool for collecting text output from external build, testing or deployment tools. It is also great for collecting other unstructured, freeform information.

## OutputProvider

OutputProvider interface defines the following methods:

* `getStandardOutput()` and `getStandardError()` — target streams to write to.
* `getLatestDate()` — return an instance of `java.util.Date` with the timestamp of the last write that happened to either standard output or standard error
* `getOutput()` — returns `java.util.Stream<TimestampedOutput>`. The output that has already happened is already available for consumption in this stream and the upcoming output will block the stream until the outputs have been closed.

`TimestampedOutput` is a simple value object that wraps the content, timestamp and the kind of output (`STDERR` or `STDOUT`) it was written to.

## Console commands

### output:list

This command lists available outputs:

```
ci@cicomponents> output:list
ID                                   | Last updated
-------------------------------------------------------------------
b928b96a-074d-4d20-b558-33e0c7b408ba | Wed Jul 20 13:11:25 ICT 2016
```

### output:tail

This command prints the output in realtime (i.e. it will print all the updates until the streams are closed):

```
ci@cicomponents> output:tail b928b96a-074d-4d20-b558-33e0c7b408ba
:licenseMain UP-TO-DATE
:licenseTest UP-TO-DATE
:license UP-TO-DATE
:cicomponents-api:compileJava
:cicomponents-api:processResources UP-TO-DATE
:cicomponents-api:classes
:cicomponents-api:jar
:cicomponents-fs-api:compileJava
:cicomponents-fs-api:processResources UP-TO-DATE
:cicomponents-fs-api:classes
:cicomponents-fs-api:jar
:cicomponents-git-api:compileJava
:cicomponents-git-api:processResources UP-TO-DATE
:cicomponents-git-api:classes
:cicomponents-git-api:jar
:cicomponents-github-api:compileJavawarning: Supported source version 'RELEASE_6' from annotation processor 'org.jvnet.hudson.annotation_indexer.AnnotationProcessorImpl' less than -source '1.8'
1 warning
...
```
