# PersistentMap

This simple interface allows to store any serializable (i.e. having a class implementing `serializable`) Java object under a String key. It can be bound using the following declaration:

```java
@Reference
protected volatile PersistentMap persistentMap;
```

`PersistentMap#getMapForBundle(bundle)` implements `Map<String, Serializable>` and is scoped to a using bundle. This prevents the leakage of the map outside of the bundle's scope.

## Console commands

### map:list

This command lists key/value pairs recorded for a bundle:

```
ci@cicomponents> pmap:list

92 - org.cicomponents.git

Key                                      | Value
-----------------------------------------------------------------------------------
6d84ab4bc08f1934c28d6b55b328c686337b6057 | 6d84ab4bc08f1934c28d6b55b328c686337b6057

155 - org.cicomponents.ci

Key          | Value
----------------------
build-status | passing

```
