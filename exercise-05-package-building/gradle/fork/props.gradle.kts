import com.neva.gradle.fork.ForkExtension

// this is quirk of Gradle - we cannot access extensions name directly in a applied file
// in a same way reading in applied file 'the<ForkExtension>().get(propertyName)'
configure<ForkExtension> {
    properties {
        define("Instance type", mapOf(
                "instanceType" to {
                    label = "Type"
                    select("local", "remote")
                    description = "Local - instance will be created on local file system\nRemote - connecting to remote instance only"
                    controller { toggle(value == "local", "instanceRunModes", "instanceJvmOpts", "localInstance*") }
                },
                "instanceAuthorHttpUrl" to {
                    label = "Author HTTP URL"
                    url("http://localhost:4502")
                    description = "For accessing AEM author instance (leave empty to do not use it)"
                }
        ))

        define("Local instance", mapOf(
                "localInstanceQuickstartJarUri" to {
                    label = "Quickstart URI"
                    description = "For file named 'cq-quickstart-x.x.x.jar'"
                },
                "localInstanceQuickstartLicenseUri" to {
                    label = "Quickstart License URI"
                    description = "For file named 'license.properties'"
                },
                "localInstanceRunModes" to {
                    label = "Run Modes"
                    text("local,nosamplecontent")
                },
                "localInstanceJvmOpts" to {
                    label = "JVM Options"
                    text("-server -Xmx2048m -XX:MaxPermSize=512M -Djava.awt.headless=true")
                },
                "localInstanceBackupDownloadUri" to {
                    label = "Backup Download URI"
                    description = "For backup file. Protocols supported: SMB/SFTP/HTTP"
                    optional()
                },
                "localInstanceBackupUploadUri" to {
                    label = "Backup Upload URI"
                    description = "For directory containing backup files. Protocols supported: SMB/SFTP"
                    optional()
                }
        ))

        define("File transfer", mapOf(
                "companyUser" to {
                    label = "User"
                    description = "Authorized to access AEM files"
                    defaultValue = System.getProperty("user.name").orEmpty()
                    optional()
                },
                "companyPassword" to {
                    label = "Password"
                    description = "For above user"
                    optional()
                },
                "companyDomain" to {
                    label = "Domain"
                    description = "Needed only when accessing AEM files over SMB"
                    defaultValue = System.getenv("USERDOMAIN").orEmpty()
                    optional()
                }
        ))
    }
}