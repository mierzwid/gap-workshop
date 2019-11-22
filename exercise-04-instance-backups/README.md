# Exercise 4: AEM backups using [Instance Plugin](https://github.com/Cognifide/gradle-aem-plugin#instance-plugin)

The setup of an AEM instance(s) can be time-consuming. Especially when we have both author and publish. Additionally, we may want to keep snapshots of a specific state of AEM instance, for reference, or to make development environments equal. This is the time when [instanceBackup](https://github.com/Cognifide/gradle-aem-plugin#task-instancebackup) task comes into play. 

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
3. and save it as a local file in projects build directory: `build/aem/instanceBackup/local/xxx.backup.zip`.

The approximate size of an author backup for AEM 6.5 is ~1.7 GB.

## Restoring

GAP does not provide you plain `instanceRestore` task. Instead, restore from a backup is done when we invoke [`instanceCreate` task](https://github.com/Cognifide/gradle-aem-plugin#task-instancecreate).
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

> Create instances from most recent backup (remote or local) or fallback to creating from scratch if there is no backup available. Default mode.`

That will work for us. let's exercise it and first destroy what we have:

`./gradlew :instanceDestroy -Pforce` (force property is needed to confirm unsafe operations).  

then type
`./gradlew :instanceSetup`

Take a look at timing. It took on my machine (MacBook Pro, 3,1 GHz Core i5, 16GB of RAM, SSD) exactly 2 minutes in comparison to over 5 minutes when starting from scratch.  
Additionally, I have the same state that was saved during backup, I have bundles, applications and content installed, etc. Saving time and reproducibility.

## Remote backups

Working with remote backups is very simple. There is only one configuration option to be specified `localInstance.backup.uploadUrl` plus credentials if those are required.

Where to backup? GAP supports SFTP & SMB protocols for uploads. For our tests, we can use Docker container with SFTP server on:

`docker run -p 2222:22 -d atmoz/sftp foo:pass:::upload`.

Lets extend `gradle/fork/gradle.user.properties.peb`, and configure file transfer for GAP:

```ini
sftpUser={{sftpUser}}
sftpPassword={{sftpPassword}}

{% if instanceType == 'local' %}
localInstance.backup.uploadUrl={{localInstanceBackupUploadUri}}
{% endif %}
```

Now we could run `./gradlew :props` to configure credentials for just run SFTP server (user: `foo`, password: `pass`) and backup upload URL (`sftp://localhost:2222/upload`). 

Fork plugin supports encryption, thus you can review `gradle.user.properties` file, it won't contain a plaintext password. The next step is to enable Gradle to decode it. We need to jump into `build.gradle.kts`:
 
```kotlin
aem {
    fileTransfer {
        sftp {
            user = forkProps["sftpUser"]
            password = forkProps["sftpPassword"]
        }
    }
}
```

Those few lines will configure credentials for all SFTP connections established by GAP.

To sum up what we just did:
1. Started docker container `atmoz/sftp` with an SFTP server on it.
2. We Configured properties UI to ask for upload URL and credentials
3. Said GAP how to decode the password using Fork plugin amenities.

Now we can test this configuration, simply type:
`./gradlew :instanceDown :instanceBackup`

What happens now? Backup goes in the same way as before, first, it prepares backup locally but additionally, in the end, it uploads the backup file to configured URL.

Now backup is accessible for download to all your teammates. Anyone at your team can have this configuration and all your backups will get uploaded to configured location. Each backup by default has timestamp included in its name this way even heavy collaboration is supported. What is convenient, when restoring from remote backup, GAP by default picks the most recent one to download so you can stay in sync!

We can perform final test. Delete local backups and restore from scratch:  
`rm -rf build/aem/instanceBackup/local/*`  
`./gradlew :instanceDestroy :instanceSetup -Pforce`
