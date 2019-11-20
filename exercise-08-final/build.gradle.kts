plugins {
    id("com.neva.fork")
    id("com.cognifide.aem.bundle")
    id("com.cognifide.aem.instance")
    id("com.cognifide.aem.tooling")
}

version = "0.0.1"
group = "com.company.workshop.aem"

apply(from = "gradle/fork.gradle.kts")

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
                "dep.kotlin"("org.jetbrains.kotlin:kotlin-osgi-bundle:1.3.50")
                "dep.acs-aem-commons"("https://github.com/Adobe-Consulting-Services/acs-aem-commons/releases/download/acs-aem-commons-4.0.0/acs-aem-commons-content-4.0.0-min.zip")
                "dep.groovy-console"("https://github.com/icfnext/aem-groovy-console/releases/download/13.0.0/aem-groovy-console-13.0.0.zip")
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
        packageCompose {
            archiveBaseName.set("workshop")
        }
        packageDeploy {
            doLast {
                aem.sync {
                    osgiFramework.restartBundle("")
                }
            }
        }
        register("groovyTest") {
            doLast {
                authorInstance.sync {
                    groovyConsole.evalCode("""
                println 'Hello world from inline code!'
            """)
                }
            }
        }
        register("sitesBackup") {
            doLast {
                authorInstance.sync {
                    repository.node("/content/example").children().forEach { siteNode ->
                        val sitePkg = packageManager.download {
                            classifier = siteNode.name
                            filter(siteNode.path)
                        }

                        fileTransfer.uploadTo(forkProps["localInstance.backup.uploadUrl"]!!, sitePkg)
                    }
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
