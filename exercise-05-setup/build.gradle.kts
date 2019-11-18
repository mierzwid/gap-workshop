plugins {
    id("com.neva.fork")
    id("com.cognifide.aem.instance")
}

version = "0.0.1"

apply(from = "gradle/fork.gradle.kts")

aem {
    tasks {
        register("sftpServer") {
            doLast {
                runDocker {
                    port(2222, 22)
                    volume(file("build/sftp").apply { parentFile.mkdirs() }, "/home/foo/upload")
                    image = "atmoz/sftp"
                    command = "foo:pass:::upload"
                }
            }
        }
    }

    fileTransfer {
        sftp {
            user = forkProps["backup.user"]
            password = forkProps["backup.password"]
        }
    }
}
