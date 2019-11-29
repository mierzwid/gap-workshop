# Exercise 3: AEM instance setup using [Instance Plugin](https://github.com/Cognifide/gradle-aem-plugin#instance-plugin)

Instance setup can be a repeatable task. Like most of such tasks, it may be good to automate it.

GAP calls itself a "Swiss army knife for AEM related automation." In the case of instance setup, it is more like a doctor with AEM specialization (aemologist). GAP knows how to diagnose AEM state, what AEM can handle and when. It is patient when it is needed and demanding when it is possible to ensure the best performance.

## Configuring file transfer

AEM instance files need to be protected by auth. We needed to specify credentials to access AEM distribution using one of 3 supported protocols (SMB, SFTP, HTTP). Credentials were configured using Fork Plugin [gradle.user.properties.peb](gradle/fork/gradle.user.properties.peb) and now are available in [gradle.users.properties](gradle.user.properties) file:

```properties
# Project specific configuration
fileTransfer.user=user.name
fileTransfer.password={nWVIC40MKSf2Z7sJwlkOXA==}
fileTransfer.domain=
```

## Understanding configuration in `gradle.user.properties`

Our generated properties file contains a section that directly configures our local AEM author instance. We skipped publish on purpose, to leave your machine a few free bytes of RAM.

Since `type` option is set to `local` other options like `runModes` and `jvmOpts` are vital.

Additionally, we have `localInstance.source` set to `auto`. GAP will then try to find the quickest way to setup our instance - preferably from backup. Knowing, there are no backups yet, we can expect the creation of a fresh instance from `cq-quickstart-6.5.0.jar` file.

```properties
instance.local-author.httpUrl=http://localhost:4502
instance.local-author.type=local
instance.local-author.runModes=local,nosamplecontent
instance.local-author.jvmOpts=-server -Xmx2048m -XX:MaxPermSize=512M -Djava.awt.headless=true

localInstance.source=auto
localInstance.quickstart.jarUrl=/Users/username/aem/6.5/cq-quickstart-6.5.0.jar
localInstance.quickstart.licenseUrl=/Users/username/aem/6.5/license.properties
```

Quite a few options and this is only one instance. Happily, we need to configure this only once.

Notice: It is also possible to configure all of this in `build.gradle.kts` file (see [docs](https://github.com/Cognifide/gradle-aem-plugin#instance-plugin)).
 
## Applying instance plugin

Add in our `build.gradle.kts`

```kotlin
plugins {
    // ...
    id("com.cognifide.aem.instance")
}
```

Generally, that's it. However, OOTB AEM instance has disabled CRXDE Lite. 
Let's enable it by defining a step in `instanceProvision` task:

```kotlin
aem {
    tasks {
        instanceProvision {
            step("enable-crxde") {
                description = "Enables CRX DE"
                condition { once() && instance.environment != "prod" }
                action {
                    sync {
                        osgiFramework.configure("org.apache.sling.jcr.davex.impl.servlets.SlingDavExServlet", mapOf(
                                "alias" to "/crx/server"
                        ))
                    }
                }
            }
        }   
    }   
}
```

Just for illustration purposes, let's also have Groovy Console and Kotlin language support OSGi bundle pre-installed on the instance. 
It could be done by simply configuring satisfy task by snippet below:

```kotlin
aem {
    tasks {
        instanceSatisfy {
            packages {
                "dep.kotlin"("org.jetbrains.kotlin:kotlin-osgi-bundle:1.3.50")
                "dep.acs-aem-commons"("https://github.com/Adobe-Consulting-Services/acs-aem-commons/releases/download/acs-aem-commons-4.0.0/acs-aem-commons-content-4.0.0-min.zip")
                "dep.groovy-console"("https://github.com/icfnext/aem-groovy-console/releases/download/13.0.0/aem-groovy-console-13.0.0.zip")
            }
        }
    }   
}
```

OK! Since instance configuration is done it's time for running the command:

`./gradlew instanceSetup`

Let's see what is happening on our filesystem. In our project folder, we can see now `.instance` directory. It contains all the AEM author files. If there would be a publish, we would see its files as well.

Frankly speaking, there is a lot more happening behind the scene. GAP is altering startup scripts, monitoring startup process, etc. 
When an instance is up, then the GAP is pre-installing CRX packages and performing instance configuration steps (provisioning). 

Now we need to wait a couple of minutes (5-10) - it is a good time for a break.

## Interactive logs monitoring

Instead of manually looking for error entries in logs, GAP could do that automatically and in an interactive manner. 

Simply run:

```bash
./gradlew instanceTail
```

And keep it running in the background. Since now, all errors occurring on running AEM instance will be immediately reported within OS notification/balloon.

Notice that this tool could observe an unlimited number of instances at once. No matter if instances are locally running or they are remote.
The only requirement is to have them accessible over HTTP protocol (no SSH required) so that GAP could poll for new log entries automatically.

### Filtering logs

It is very important to listen only to the logs which you are interested in. Otherwise, you would get flooded with a cannonade of unimportant notifications. To filter logs simply edit `gradle/instanceTail/incidentFilter.txt` file and add there log message or only a part of it. Logs that would match those lines will get filtered out. You can use wildcards as well, try:

```text
org.apache.felix.metatype Missing element * in element OCD
```

## Control tasks

Let's review all the tasks available:
`./gradlew tasks`

Wow, quite a few! Feel free to play with them :-)

```
AEM tasks
---------
await - Await for healthy condition of AEM instances and/or virtualized AEM environment.
debug - Dumps effective AEM build configuration of project to JSON file
destroy - Destroys local AEM instance(s) and virtualized AEM environment.
down - Turns off AEM local instances and/or virtualized AEM environment.
instanceAwait - Await for healthy condition of all AEM instances.
instanceBackup - Turns off local instance(s), archives to ZIP file, then turns on again.
instanceCreate - Creates local AEM instance(s).
instanceDestroy - Destroys local AEM instance(s).
instanceDown - Turns off local AEM instance(s).
instanceProvision - Configures instances only in concrete circumstances (only once, after some time etc)
instanceReload - Reloads all AEM instance(s).
instanceResetup - Destroys then sets up local AEM instance(s).
instanceResolve - Resolves instance files from remote sources before running other tasks
instanceRestart - Turns off then on local AEM instance(s).
instanceSatisfy - Satisfies AEM by uploading & installing dependent packages on instance(s).
instanceSetup - Creates and turns on local AEM instance(s) with satisfied dependencies and application built.
instanceTail - Tails logs from all configured instances (local & remote) and notifies about unknown errors.
instanceUp - Turns on local AEM instance(s).
resetup - Destroys then sets up local AEM instance(s) and/or virtualized AEM environment.
resolve - Resolves all files from remote sources before running other tasks.
restart - Turns off then on AEM local instances and/or virtualized AEM environment.
setup - Sets up local AEM instance(s) and/or virtualized AEM environment.
up - Turns on AEM local instances and/or virtualized AEM environment.
```
