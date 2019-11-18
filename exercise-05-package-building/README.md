# Exercise 5: Building CRX packages 

Basically, building CRX package is simple. We need to put into ZIP JCR content and Vault metadata. And also ensure to append build OSGi bundle(s).
Things are getting more complicated if we want to extract some CRX package contents to separate packages.
So sometimes we want to have all-in-one package (which installation is fastest) and sometimes we want to skip reinstalling some contents to prevent losing content on running AEM instance.

GAP is addressing such requirements in a way of full dynamism in composing CRX packages from multiple JCR roots, bundles, etc without a need for restructuring a project.

## Building CRX package with content only

Add in `build.gradle.kts`

```kotlin
plugins {
    // ...
    id("com.cognifide.aem.package")
}
```

Then let's create some package contents, in detail, create files:

*src/main/content/jcr_root/content/dam/workshop/.content.xml*:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jcr:root xmlns:sling="http://sling.apache.org/jcr/sling/1.0" xmlns:jcr="http://www.jcp.org/jcr/1.0" xmlns:nt="http://www.jcp.org/jcr/nt/1.0"
    jcr:primaryType="sling:Folder">
    <jcr:content
        jcr:primaryType="nt:unstructured"
        jcr:title="Workshop"/>
</jcr:root>
```

*src/main/content/META-INF/vault/filter.xml*:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<workspaceFilter version="1.0">
    <filter root="/content/dam/workshop"/>
</workspaceFilter>
```

That's it. Rest of package metadata files (like *vault/properties.xml*) will be automatically generated by GAP :)

Let's build a package:

```bash
./gradlew :packageCompose
```

See that, built package name derived name from project name (*exercise-05-package-building*). We could override it by writing:

```kotlin
aem {
    tasks {
        packageCompose {
            archiveBaseName.set("workshop")
        }   
    }   
}
```

Still, version, classifier, extension remains same. That's because `archiveBaseName` instead of `archiveName` was overridden.
It is good to know the difference!

Notice that, in the end of building package, it is automatically validated.
Validation requires up-to-date node types definitions stored in built package. 
However, this kind of file is also synchronized from running AEM instance automatically.

During regular development, just remember to save generated file in VCS (*gradle/package/nodetypes.sync.cnd*) 
so that building CRX package without any AEM instance available (e.g on Jenkins) will be also possible.

## Building CRX package with OSGi bundle

As of bundle plugin is extending package plugin, now we need to replace:

```kotlin
plugins {
    // ...
    id("com.cognifide.aem.package")
}
```

with:

```kotlin
plugins {
    // ...
    id("com.cognifide.aem.bundle")
}
```

Be aware that bundle plugin under the neath is applying official Gradle Java Plugin and GAP package plugin.
Its main functionality is too run [BND tool](https://bnd.bndtools.org/) to generate OSGi specific attributes for built JAR.

GAP is strongly using paradigm *convention over configuration*. Once we specify:

```
group = "com.company.workshop.aem"
```

Then all other values OSGi specific attributes in MANIFEST.mf file like: 
*Bundle-SymbolicName*, *Export-Package*, *Sling-Model-Packages* and more will be automatically assigned.

So that the only thing left is to add some code, let's create some OSGi service:

*src/main/java/com/company/workshop/aem/HelloService.java*:

```java
package com.company.workshop.aem;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Activate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = HelloService.class)
public class HelloService extends SlingFilter {

    private static final Logger LOG = LoggerFactory.getLogger(HelloService.class);

    @Activate
    protected void activate() {
        LOG.info("Hello service called!");
    }
}

```

And declare compile time dependencies and repositories from which they can be downloaded:

```kotlin
repositories {
    jcenter()
    maven { url = uri("https://repo.adobe.com/nexus/content/groups/public") }
}

dependencies {
    compileOnly( "org.osgi:osgi.cmpn:6.0.0")
    compileOnly( "org.osgi:org.osgi.core:6.0.0")
    compileOnly( "javax.servlet:servlet-api:2.5")
    compileOnly( "javax.servlet:jsp-api:2.0")
    compileOnly( "javax.jcr:jcr:2.0")
    compileOnly( "org.slf4j:slf4j-api:1.7.25")
    compileOnly( "org.apache.geronimo.specs:geronimo-atinject_1.0_spec:1.0")
    compileOnly( "org.apache.sling:org.apache.sling.api:2.16.4")
    compileOnly( "org.apache.sling:org.apache.sling.jcr.api:2.4.0")
    compileOnly( "org.apache.sling:org.apache.sling.models.api:1.3.6")
    compileOnly( "org.apache.sling:org.apache.sling.settings:1.3.8")
    compileOnly( "com.google.guava:guava:15.0")
    compileOnly( "com.google.code.gson:gson:2.8.2")
    compileOnly( "joda-time:joda-time:2.9.1")

    compileOnly("com.adobe.aem:uber-jar:6.5.0:apis")
}
```

Let's build an OSGi bundle only:

```bash
./gradlew :bundleCompose
```

To build CRX package containing that bundle, simply write as previously:

```bash
./gradlew :packageCompose
```

## Deploying CRX packages

Simply write:

```bash
./gradlew :packageDeploy
```

As of deploy task depends on compose, we could omit building CRX packages before running task `packageDeploy`. It will be done automatically.

Notice that, task `packageDeploy` could work longer than when deploying CRX package using Maven and Vault Content Package Plugin.
It is connected with advanced AEM instance stability checking which is performed by GAP after each CRX package deployment.

To disable it, simply append `-Ppackage.deploy.awaited=false`, but then do not be surprised when you will don't know when your application is ready to use.
GAP instance checking is helping in detecting that moment to be able to work more comfortably and preventing requesting built AEM pages when our application is not stable.