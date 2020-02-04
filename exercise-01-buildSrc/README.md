# Exercise 1: Configure plugins - buildSrc

## Plugin repositories

```kotlin
repositories {
    jcenter()
    gradlePluginPortal()

    // For GAP
    maven("http://dl.bintray.com/cognifide/maven-public")
    // For Fork
    maven("https://dl.bintray.com/neva-dev/maven-public")
}
```

## Plugins and their versions

```kotlin
dependencies {
    // Of course :-) Gradle AEM Plugin.
    implementation("com.cognifide.gradle:aem-plugin:10.1.5")
    // Then Gradle Fork Plugin to work with lots of properties we will need.  
    implementation("com.neva.gradle:fork-plugin:4.2.0")
    // Since we use Kotlin DSL in Gradle, as mentioned above
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.61")
}
```    

## Test

Now let apply GAP common and Fork plugins and check if we can configure them:

```kotlin
plugins {
    id("com.neva.fork")
    id("com.cognifide.aem.common")
}

aem {
    // configuration to be added later
}

fork {
    // configuration to be added later
}
```

Run `./gradlew tasks` and check for "Aem tasks" and "Fork tasks" sections.