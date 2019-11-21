# Exercise 2: Fork Plugin

We use the [Fork Plugin](https://github.com/neva-dev/gradle-fork-plugin) as the "Interactive gradle.user.properties file generator". 

## Create user-specific properties file template

```
mkdir -p gradle/fork
vim gradle/fork/gradle.user.properties.peb
```

```pebble
# Project specific configuration
adUser={{adUser}}
adPassword={{adPassword}}
adDomain={{adDomain}}

# AEM configuration
notifier.enabled=true

{% if instanceAuthorHttpUrl is not empty %}
instance.local-author.httpUrl={{instanceAuthorHttpUrl}}
instance.local-author.type={{instanceType}}
instance.local-author.runModes={{instanceRunModes}}
instance.local-author.jvmOpts={{instanceJvmOpts}}
{% endif %}

{% if instanceType == 'local' %}
localInstance.source={{ localInstanceSource }}
localInstance.quickstart.jarUrl={{ localInstanceQuickstartJarUri }}
localInstance.quickstart.licenseUrl={{ localInstanceQuickstartLicenseUri }}
{% endif %}
```

## Configure properties templating

```
vim gradle/fork.gradle.kts
```

```kotlin
import com.cognifide.gradle.aem.common.instance.local.Source
import com.neva.gradle.fork.ForkExtension

// this is quirk of Gradle - we cannot access extensions name directly in a applied file
// in a same way reading in applied file 'the<ForkExtension>().get(propertyName)'
configure<ForkExtension> {
    properties {
        define(mapOf(
                "adUser" to {
                    description = "AD user" 
                    defaultValue = System.getProperty("user.name")
                },
                "adPassword" to {
                    description = "AD password"
                    password()
                },  
                "adDomain" to {
                    description = "AD domain"
                },           
                "instanceAuthorHttpUrl" to {
                    url("http://localhost:4502")
                    optional()
                    description = "URL for accessing AEM author instance"
                },
                "instanceType" to {
                    select("local", "remote")
                    description = "local - instance will be created on local file system\nremote - connecting to remote instance only"
                    controller { toggle(value == "local", "instanceRunModes", "instanceJvmOpts", "localInstance*") }
                },
                "instanceRunModes" to { text("local,nosamplecontent") },
                "instanceJvmOpts" to { text("-server -Xmx2048m -XX:MaxPermSize=512M -Djava.awt.headless=true") },
                "localInstanceSource" to {
                    description = "Controls how instances will be created (from scratch, backup or automatically determined)"
                    select(Source.values().map { it.name.toLowerCase() }, Source.AUTO.name.toLowerCase())
                },
                "localInstanceQuickstartJarUri" to {
                    description = "Quickstart JAR (cq-quickstart-x.x.x.jar)"
                },
                "localInstanceQuickstartLicenseUri" to {
                    description = "Quickstart license file (license.properties)"
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

Also notice that password are automatically encrypted by Fork plugin. Later on, we will need to decode them before using.
