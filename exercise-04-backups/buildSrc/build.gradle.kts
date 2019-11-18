repositories {
    gradlePluginPortal()
    maven { url = uri("http://dl.bintray.com/cognifide/maven-public") }
    maven { url = uri("https://dl.bintray.com/neva-dev/maven-public") }
}

dependencies {
    implementation("com.cognifide.gradle:aem-plugin:9.0.2")
    implementation("com.neva.gradle:fork-plugin:4.0.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.50")
}
