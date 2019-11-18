plugins {
    id("com.neva.fork")
    id("com.cognifide.aem.instance")
}

version = "0.0.1"

apply(from = "gradle/fork.gradle.kts")

aem {
    fileTransfer {
        sftp {
            user = forkProps["backup.user"]
            password = forkProps["backup.password"]
        }
    }
}
