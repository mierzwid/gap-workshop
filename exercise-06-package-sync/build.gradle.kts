plugins {
    id("com.neva.fork")
    id("com.cognifide.aem.instance")
    id("com.cognifide.aem.bundle")
    id("com.cognifide.aem.tooling")
}

version = "1.0.0"
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
        packageCompose {
            archiveBaseName.set("workshop")
            fromJar("org.jsoup:jsoup:1.10.2")
            fromJar("com.github.mickleroy:aem-sass-compiler:1.0.1")
        }
        bundleExportEmbed("org.hashids:hashids:1.0.1", "org.hashids")
    }
}
