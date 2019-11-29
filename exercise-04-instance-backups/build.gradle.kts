plugins {
    id("com.neva.fork")
    id("com.cognifide.aem.instance")
}

version = "1.0.0"

apply(from = "gradle/fork.gradle.kts")

repositories {
    jcenter()
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
    }
}
