import com.neva.gradle.fork.PropsExtension

plugins {
    id("com.neva.fork")
    id("com.cognifide.aem.common")
    id("com.cognifide.aem.instance")
}

version = "0.0.1"

apply(from = "gradle/properties.gradle.kts")

aem {
    fileTransfer {
        sftp {
            user = the<PropsExtension>().get("backup.user")
            password = the<PropsExtension>().get("backup.password")
        }
    }
}
