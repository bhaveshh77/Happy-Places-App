pluginManagement {
    repositories {
        google()
        mavenCentral()  f aaaaaaa
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "HappyPlaces"
include(":app")
