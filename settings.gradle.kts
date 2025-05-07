pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        maven("https://us-central1-maven.pkg.dev/varabyte-repos/public")
        mavenCentral()
    }
}

rootProject.name = "tfg"

include(":site")
include(":androidapp")
