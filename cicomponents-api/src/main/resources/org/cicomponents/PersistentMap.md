# PersistentMap

This simple interface allows to store any serializable (i.e. having a class implementing `serializable`) Java object under a String key. It can be bound using the following declaration:

```java
@Reference
protected volatile PersistentMap persistentMap;
```

`PersistentMap` implements `Map<String, Serializable>`

## Console commands

### map:list

This command lists key/value pairs recorded:

```
ci@cicomponents> map:list
Key                                                                                   | Value
----------------------------------------------------------------------------------------------------------------------------------
github-pr-status-cicomponents-cicomponents-7-7e9e5890e0ac71913ee0961dd730fe3d5b35b176 | Wed Jul 20 14:24:34 ICT 2016
github-pr-status-cicomponents-cicomponents-6-fe5eac22c73828abc240a4481536252208ee3c83 | Wed Jul 20 14:19:27 ICT 2016
e9ea6783cbc071cdfbf3                                                                  | [1f2029ee38990575c7e68e8393480349a3856aca]
github-pr-status-cicomponents-cicomponents-6-8c55fa8990a844cf17484f317970d8dc73aae4a1 | Wed Jul 20 14:21:07 ICT 2016
793ab61b82808bca8e8cebc147730f4d2c1dac64                                              | 793ab61b82808bca8e8cebc147730f4d2c1dac64
0dd68708d7bf7ff1887af004876d0212b797edb2                                              | 0dd68708d7bf7ff1887af004876d0212b797edb2
561fe804eba8469e4c36e5e9fd6503ba0eb9d60d                                              | 561fe804eba8469e4c36e5e9fd6503ba0eb9d60d
```
