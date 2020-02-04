# Exercise 2: Fork Plugin

We use the [Fork Plugin](https://github.com/neva-dev/gradle-fork-plugin) as the "Interactive gradle.user.properties file generator". 

## Create user-specific properties file template

```
mkdir -p gradle/fork
vim gradle/fork/gradle.user.properties.peb
```

```pebble
notifier.enabled=true

fileTransfer.user={{companyUser}}
fileTransfer.password={{companyPassword}}
fileTransfer.domain={{companyDomain}}

instance.local-author.httpUrl={{instanceAuthorHttpUrl}}
instance.local-author.type={{instanceType}}

{% if instanceType == 'local' %}
localInstance.quickstart.jarUrl={{ localInstanceQuickstartJarUri }}
localInstance.quickstart.licenseUrl={{ localInstanceQuickstartLicenseUri }}
{% endif %}
```

## Configure properties templating

```
vim gradle/fork/props.gradle.kts
```

```kotlin
import com.neva.gradle.fork.ForkExtension

// this is quirk of Gradle - we cannot access extensions name directly in a applied file
// in a same way reading in applied file 'the<ForkExtension>().get(propertyName)'
configure<ForkExtension> {
    properties {
        define("Instance type", mapOf(
                "instanceType" to {
                    label = "Type"
                    select("local", "remote")
                    description = "Local - instance will be created on local file system\nRemote - connecting to remote instance only"
                    controller { toggle(value == "local", "instanceRunModes", "instanceJvmOpts", "localInstance*") }
                },
                "instanceAuthorHttpUrl" to {
                    label = "Author HTTP URL"
                    url("http://localhost:4502")
                    description = "For accessing AEM author instance (leave empty to do not use it)"
                }
        ))

        define("Local instance", mapOf(
                "localInstanceQuickstartJarUri" to {
                    label = "Quickstart URI"
                    description = "For file named 'cq-quickstart-x.x.x.jar'"
                },
                "localInstanceQuickstartLicenseUri" to {
                    label = "Quickstart License URI"
                    description = "For file named 'license.properties'"
                }
        ))

        define("File transfer", mapOf(
                "companyUser" to {
                    label = "User"
                    description = "Authorized to access AEM files"
                    defaultValue = System.getProperty("user.name").orEmpty()
                    optional()
                },
                "companyPassword" to {
                    label = "Password"
                    description = "For above user"
                    optional()
                },
                "companyDomain" to {
                    label = "Domain"
                    description = "Needed only when accessing AEM files over SMB"
                    defaultValue = System.getenv("USERDOMAIN").orEmpty()
                    optional()
                }
        ))
    }
}
```

## Test properties setup

`./gradlew :props`

Please specify required credentials and paths to AEM 6.5 distribution and licence and execute. As a result new `gradle.user.properties` file should be generated with properties values.

Thanks to that, each new developer will have easy start joining your project despite the fact that we need to configure a lot.

If you noticed that we could commit `gradle.user.properties` file - nice catch. Unfortunately, usually this file contains user credentials (we will see examples later on), so it is better to leave it outside VCS (put it in *.gitignore* file).

Also notice that password are automatically encrypted by Fork plugin.
