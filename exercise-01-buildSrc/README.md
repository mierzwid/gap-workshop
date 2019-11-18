# Exercise 1: Configure plugins - buildSrc

## Kotlin DSL plugin

Since we use Kotlin DSL in Gradle we wil configure Kotlin DSL plugin to skip experimental API warnings, since GAP uses Coroutines API.
```kotlin
plugins {
    `kotlin-dsl`
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.50")
}
```

## Other plugins and their versions

```kotlin
dependencies {
    // Of course :-) Gradle AEM Plugin.
    implementation("com.cognifide.gradle:aem-plugin:9.0.3")
    // Then Gradle Fork Plugin to work with lots of properties we will need.  
    implementation("com.neva.gradle:fork-plugin:4.0.1")
    // Since we use Kotlin DSL in Gradle, as mentioned above
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.50")
}
```    

## Plugin repositories

```kotlin
repositories {
    gradlePluginPortal()
    // For GAP
    maven { url = uri("http://dl.bintray.com/cognifide/maven-public") }
    // For Fork
    maven { url = uri("https://dl.bintray.com/neva-dev/maven-public") }
}
```

## Test

Now lets apply GAP and Fork plugins and check if we can configure them:

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
