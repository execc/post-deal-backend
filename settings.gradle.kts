pluginManagement {
    val mavenUser: String by settings
    val mavenPassword: String by settings
    repositories {
        gradlePluginPortal()
        mavenLocal()
        mavenCentral()
        maven {
            name = "maven-snapshots"
            url = uri("https://artifacts.vostokservices.com/repository/maven-snapshots/")
            mavenContent {
                snapshotsOnly()
            }
            credentials {
                username = mavenUser
                password = mavenPassword
            }
        }
        maven {
            name = "maven-releases"
            url = uri("https://artifacts.vostokservices.com/repository/maven-releases/")
            mavenContent {
                releasesOnly()
            }
            credentials {
                username = mavenUser
                password = mavenPassword
            }
        }
    }
}

rootProject.name = "deals-app"

include(
    "deals-webapp:deals-webapp-api",
    "deals-webapp:deals-webapp-app",

    "deals-contract:deals-contract-api",
    "deals-contract:deals-contract-app",

    "deals-domain"
)