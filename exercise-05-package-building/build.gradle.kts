plugins {
    id("com.neva.fork")
    id("com.cognifide.aem.instance")
}

version = "0.0.1"

apply(from = "gradle/fork.gradle.kts")

aem {
    tasks {
        instanceProvision {
            step("enable-crxde") {
                description = "Enables CRX DE"
                condition { once() && instance.environment != "prod" }
                action {
                    sync {
                        osgiFramework.configure("org.apache.sling.jcr.davex.impl.servlets.SlingDavExServlet", mapOf(
                                "alias" to "/crx/server"
                        ))
                    }
                }
            }
        }
        instanceSatisfy {
            packages {
                "dep.kotlin" { resolve("org.jetbrains.kotlin:kotlin-osgi-bundle:1.3.50") }
                "dep.acs-aem-commons" { download("https://github.com/Adobe-Consulting-Services/acs-aem-commons/releases/download/acs-aem-commons-4.0.0/acs-aem-commons-content-4.0.0-min.zip") }
            }
        }
        register("sftpServer") {
            doLast {
                runDocker {
                    port(2222, 22)
                    volume(file("build/sftp").apply { mkdirs() }, "/home/foo/upload")
                    image = "atmoz/sftp"
                    command = "foo:pass:::upload"
                }
            }
        }
    }
    fileTransfer {
        credentials(forkProps["adUser"], forkProps["adPassword"], forkProps["adDomain"])

        sftp {
            user = forkProps["sftpUser"]
            password = forkProps["sftpPassword"]
        }
    }
}
