pluginManagement {
    repositories {
        google()  // Removed content filtering to ensure KSP plugin can be found
        mavenCentral()
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

rootProject.name = "Restaurant Finder"
include(":app")
