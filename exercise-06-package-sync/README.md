# Exercise 6: Syncing JCR content with running AEM instance

Assembling *.content.xml* files by copy-paste technique could be error-prone and not enough productive.
Instead, GAP has a quick mechanism for automatic synchronization of these files from running AEM instance.
What is more, these XML files are normalized - redundant properties and unused XML namespaces are being removed, etc.

## Using content synchronization task

Add in `build.gradle.kts`

```kotlin
plugins {
    // ...
    id("com.cognifide.aem.package.sync")
}
```

Now task named `packageSync` is available to use. Simply run it:

```bash
./gradlew :packageSync
```

GAP supports 2 types of JCR content transports. 

* *package_download* - on-the-fly CRX package creation with downloading & unpacking
* *vlt_checkout* - using Vault checkout command

By default, GAP is using *package_download* transport as it is much quicker because Vault is transferring files one by one using multiple HTTP requests. Package download is using AEM instance to create an appropriate package and limit the number of connections.

Try playing with VLT checkout mode and compare results:

```
./gradlew :packageSync -Ppackage.sync.type=vlt_checkout
```

The results should be the same, but the time consumed... The GAP default transport method is much more effective.

## Alternative filter for content synchronization

If there is an available file named *filter.sync.xml* it has precedence in case of content synchronization over file *filter.xml*
used when installing CRX package.

It could be used e.g to filter out DAM rendition files.
Simply copy origin file *filter.xml* then specify exclusions.

*src/main/content/META-INF/vault/filter.sync.xml*

```xml
<?xml version="1.0" encoding="UTF-8"?>
<workspaceFilter version="1.0">
    <filter root="/content/dam/workshop">
        <include pattern="/content/dam/workshop(/.*)?" />
        <exclude pattern="/content/dam/workshop/.*/renditions/.*"/>
        <include pattern="/content/dam/workshop/.*/renditions/original(.*)?"/>
    </filter>
</workspaceFilter>

```


## Checking any nodes specified via command line property

Simply use property:

```
./gradlew :packageSync -Pfilter.roots=[/content/dam/workshop]
```