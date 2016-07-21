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
ci@cicomponents> map:list 97
Key                                      | Value
-----------------------------------------------------------------------------------
9e914d735c1bc4405cac9ceb7c4fb1c345ff4629 | 9e914d735c1bc4405cac9ceb7c4fb1c345ff4629
```
