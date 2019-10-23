import com.neva.gradle.fork.PropsExtension

plugins {
    id("com.neva.fork")
    id("com.cognifide.aem.common")
    id("com.cognifide.aem.instance")
}

version = "0.0.1"

apply(from = "gradle/fork.gradle.kts")

aem {
    fileTransfer {
        smb {
            user = the<PropsExtension>().get("smb.user")
            password = the<PropsExtension>().get("smb.password")
            domain = "DOMAIN_NAME"
        }
    }
}
