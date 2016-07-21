# Resource Listener

This is a very simple interface that should be implemented in order to receive [resource holders](ResourceHolder.md) from [resource emitters](ResourceEmitter.md).

Due to limitations related to type erasure in Java, only one specific type of events can be listened to by one class (arguably leading to a clean separation of listeners):

```java
@Component(property = {"type=latest", "repository=https://github.com/cicomponents/cicomponents", "branch=master"})
public class MasterListener implements ResourceListener<GitRevision> {
  void onEmittedResource(ResourceHolder<T> holder,
                         ResourceEmitter<T> emitter) {
    /// ...
 }
}
```
