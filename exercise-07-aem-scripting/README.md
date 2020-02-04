# Exercise 7: Custom AEM tasks (scripting)

`aem` object consists of complete API related to AEM on which all tasks are based. We have no limitations to implement own logic to perform any automation around AEM. Below tasks are just examples of using Gradle AEM DSL.

## Restart Scene7 bundle after deploying CRX package

```kotlin
aem {
    tasks {
        packageDeploy {
            doLast {
                syncInstances {
                    osgiFramework.restartBundle("com.adobe.cq.dam.cq-scene7-imaging")
                }
            }
        }   
    }   
}
```

## Eval some Groovy Code on AEM instance

The code could be specified directly or inside a file.
Let's create a sample script in file *gradle/groovyScript/hello.groovy*:

```groovy
println 'Hello world from inside separate file!'
```

Then let's reference it in *build.gradle.kts*:

```kotlin
aem {
    tasks {
        register("groovyTest") {
            doLast {
                authorInstance.sync {
                    groovyConsole.evalCode("""
                        println 'Hello world from inline code!'
                    """)

                    groovyConsole.evalScript("hello.groovy")
                } 
            }       
        }   
    }   
}
```

## Backup all sites stored on AEM instance

The example below demonstrates how to create a backup of content when we do not know which repository paths should be considered.
GAP offers easy to use `repository` instance service allowing CRUD operations and traversing content. 
It also provides `packageManager` instance service which has method `download` useful for building CRX packages on-the-fly.

```kotlin
aem {
    tasks {
        register("contentBackup") {
            doLast {
                authorInstance.sync {
                    repository.node("/content/dam").children().forEach { siteNode ->
                        val sitePkg = packageManager.download {
                            classifier = siteNode.name
                            filter(siteNode.path)
                        }
                        // val sitePkg = siteNode.download() // other shorthand

                        fileTransfer.uploadTo(forkProps["localInstance.backup.uploadUrl"]!!, sitePkg)
                    }
                }
            }
        }   
    }   
}
```
