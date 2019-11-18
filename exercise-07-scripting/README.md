# Exercise 7: Custom AEM tasks (scripting)

As of `aem` object consists of complete API related with AEM on which
all tasks are based, we have no limitations to implement own logic to perform any automation around AEM.

Below tasks are just examples of using Gradle AEM DSL.

## Restart Scene7 bundle after deploying CRX package

```kotlin
aem {
    tasks {
        packageDeploy {
            doLast {
                sync {
                    osgiFramework.restartBundle("") // put scene7 bundle symbolic name here
                }   
            }       
        }   
    }   
}
```

## Eval some Groovy Code on AEM instance

Code could be specified directly or inside file.
Let's create sample script in file *gradle/groovyScript/hello.groovy*:

```groovy
println 'Hello world from inside separate file!'
```

Then once again in *build.gradle.kts*:

```kotlin
aem {
    tasks {
        register("groovyTest") {
            doLast {
                authorInstance.sync {
                    groovyConsole.evalCode("""
                        println 'Hello world from inline code!'
                    """)
                } 
            }       
        }   
    }   
}
```

## Backup all sites stored on AEM instance

```kotlin
aem {
    tasks {
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
}
```
