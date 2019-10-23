# Exercise 3: AEM backups using [Instance Plugin](https://github.com/Cognifide/gradle-aem-plugin#instance-plugin)

Setup of an AEM instance(s) can be time consuming. Especially when we have both author and publish. Additionally, we may want to keep snapshots of a specific state of AEM instance, for reference, or to make development environments equal. This is time when [instanceBackup](https://github.com/Cognifide/gradle-aem-plugin#task-instancebackup) task comes into play. 

## Local backups

Performing local backup is straightforward. We don't need to have any additional configuration. There is only one requirement, we need to have at least one extracted AEM instance.

To give our backup files nice names, lets configure version for our project (`build.gradle.kts`):

`version = "0.0.1"` 

Then simply type:

`./gradlew :instanceDown :instanceBackup`

Again, like everything connected to whole AEM instance management, it will take time. 

What happens? 
1. If you have your author on then GAP will take it down (`:instanceDown`), 
2. then it will compress entire `.instance` folder to snapshot all your instances 
3. and save it as local file in projects build directory: `build/aem/instanceBackup/local/xxx.backup.zip`.

The approximate size of an author backup for AEM 6.5 is ~1.7 GB.

## Restoring

GAP does not provide you plain "instanceRestore" task. Instead, restore from a backup is done when we invoke [`instanceCreate` task](https://github.com/Cognifide/gradle-aem-plugin#task-instancecreate).
Let's see what `:instanceSetup` is actually doing:

`./gradlew :instanceSetup --dry-run`

```
./gradlew :instanceSetup --dry-run
:instanceCreate SKIPPED
:instanceUp SKIPPED
:instanceSatisfy SKIPPED
:instanceProvision SKIPPED
:instanceSetup SKIPPED

BUILD SUCCESSFUL in 814ms
```

Yes, it is invoking `intanceCreate` plus applying some additional instance configuration which we will cover later on.

To be more specific, `instanceCreate` takes into consideration `localInstance.source` property. In our case it is set to `auto` which means:

"Create instances from most recent backup (remote or local) or fallback to creating from the scratch if there is no backup available. Default mode."

That will work for us. let's exercise it and first destroy what we have:

`./gradlew :instanceDestroy -Pforce` - `-Pforce` is needed to confirm unsafe operations   

then type
`./gradlew :instanceSetup`

Take a look at timing. It took on my machine (MacBook Pro, 3,1 GHz Core i5, 16GB of RAM, SSD) exactly 2 minutes in comparison to over 5 minutes when starting from scratch.
Additionally, I have exactly the same state that was saved during backup, I might have bundles, applications and content installed, etc. Saving time and reproducibility.

## Remote backups

Working with remote backups is just as simple. There is only one configuration option to be specified `localInstance.backup.uploadUrl`

Let's extend our properties template `gradle/fork/gradle.properties.peb` adding this option:

```properties
{% if instanceType == 'local' %}
...
localInstance.backup.uploadUrl={{localInstanceBackupUploadUri}}
{% endif %}
```


