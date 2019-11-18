# Exercise 6: Syncing JCR content with running AEM instance

Assembling *.content.xml* files by copy-paste technique could be error-prone and not enough productive.
Instead, GAP has quick mechanism for automatic synchronization of these files from running AEM instance.
What is more, these XML files are normalized - redundant properties and unused XML namespaces are being removed etc.

## Using content synchronization task

Add in `build.gradle.kts`

```kotlin
plugins {
    // ...
    id("com.cognifide.aem.tooling")
}
```

Now task named `sync` is available to use. Simply run it:

```bash
./gradew :sync
```

GAP supports 2 types of JCR content transports. 

* *package_download* - on-the-fly CRX package creation with downloading & unpacking
* *vlt_checkout* - using Vault checkout command

By default, GAP is using *package_download* transport as of it is much more quick,
because Vault is transferring files one by one using multiple HTTP requests.
Package download is using AEM instance to create appropriate package so that it is much less using network.

Try playing with VLT checkout mode and compare results:

```
./gradlew :sync -Psync.mode=vlt_checkout
```

The results should be same, but time consumed... GAP default transport method is much more effective.

## Alternative filter for content synchronization

If there is available file named *filter.sync.xml* it has precedence in case of content synchronization over file *filter.xml*
used when installing CRX package.

It could be used e.g to filter out DAM rendition files.

*src/main/content/META-INF/vault/filter.sync.xml*

```xml

```
