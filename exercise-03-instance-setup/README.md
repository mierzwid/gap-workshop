# Exercise 3: AEM instance setup using [Instance Plugin](https://github.com/Cognifide/gradle-aem-plugin#instance-plugin)

Instance setup can be repeatable task. As most of repeatable tasks, it may be good to automate it.

GAP calls itself "Swiss army knife for AEM related automation." In case of instance setup, it is more like a doctor with AEM specialization (aemologist). GAP knows how to diagnose AEM state, what AEM can handle and when. It is patient when it is needed and demanding when it is possible to ensure best performance.

## Configuring file transfer

As of AEM instance files need to be protected by auth, we need to specify credentials according to instance file URLs.

To configure same credentials for protocols SMB, SFTP and HTTP with basic auth, append snippet below to *build.gradle.kts*:

```kotlin
aem {
    fileTransfer {
        credentials(forkProps["adUser"], forkProps["adPassword"], forkProps["adDomain"])
    }
}
```

Notice that accessing properties by `forkProps[name]` (instead of `project.findProperty(name)`) automatically runs decryption of property value (if it was encrypted).

## Understanding configuration in `gradle.user.properties`

Our generated properties file contains section which directly configures our local AEM author instance. We skipped publish on purpose, to leave your machine few free bytes of RAM.

Since `type` option is set to `local` other options like `runModes` and `jvmOpts` are vital.

Additionally, we have `localInstance.source` set to `auto`. GAP will then try to find the quickest way to setup our instance - preferably from backup. Knowing, there are no backups yet, we can expect creation of a fresh instance from `cq-quickstart-6.5.0.jar` file.

```properties
instance.local-author.httpUrl=http://localhost:4502
instance.local-author.type=local
instance.local-author.runModes=local,nosamplecontent
instance.local-author.jvmOpts=-server -Xmx2048m -XX:MaxPermSize=512M -Djava.awt.headless=true

localInstance.source=auto
localInstance.quickstart.jarUrl=/Users/username/aem/6.5/cq-quickstart-6.5.0.jar
localInstance.quickstart.licenseUrl=/Users/username/aem/6.5/license.properties
```

Quite a few options, and this is only one instance. Happily, we need to configure this only once.

Notice: It is also possible to configure all of this in `build.gradle.kts` file (see [docs](https://github.com/Cognifide/gradle-aem-plugin#instance-plugin)).
 
## Applying instance plugin

Add in our `build.gradle.kts`

```kotlin
plugins {
    // ...
    id("com.cognifide.aem.instance")
}
```

Generally, that's it. However OOTB AEM instance has disabled CRXDE Lite. 
Let's enable it by defining step in `instanceProvision` task:

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

Just for illustration purposes, let's also have Groovy Console and Kotlin language support OSGi bundle pre-installed on instance. 
It could be done by simply configuring satisfy task by snippet below:

```kotlin
aem {
    tasks {
        instanceSatisfy {
            packages {
                "dep.kotlin" { resolve("org.jetbrains.kotlin:kotlin-osgi-bundle:${Build.KOTLIN_VERSION}") }
                "dep.acs-aem-commons" { download("https://github.com/Adobe-Consulting-Services/acs-aem-commons/releases/download/acs-aem-commons-4.0.0/acs-aem-commons-content-4.0.0-min.zip") }
                "dep.groovy-console" { get("https://github.com/icfnext/aem-groovy-console/releases/download/14.0.0/aem-groovy-console-14.0.0.zip") }
            }
        }
    }   
}
```

OK! Now instance configuration is done. Now it's time for running command:

`./gradlew instanceSetup`

Let's see what is happening on our HDD. In our project folder we can see now `.instance` directory. It contains all the AEM author files. It there would be a publish, we would see its files as well.
Frankly speaking, there is a lot more happening behind the scene. GAP is altering startup scripts, monitoring startup process, etc. 
When instance is up, then GAP is pre-installing CRX packages and performing instance configuration steps (provisioning). 

Now we need to wait couple of minutes (5-10) - it is good time for a break.

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

