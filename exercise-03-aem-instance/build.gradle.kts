plugins {
    id("com.neva.fork")
    id("com.cognifide.aem.common")
    id("com.cognifide.aem.instance")
}

apply(from = "gradle/fork.gradle.kts")

aem {
    // configuration to be added later
}
