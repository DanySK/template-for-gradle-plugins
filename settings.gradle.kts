pluginManagement {
    plugins {
        kotlin("jvm") version "1.5.31"
        id("com.gradle.plugin-publish") version "0.16.0"
        id("org.jetbrains.dokka") version "1.5.30"
        id("org.danilopianini.git-sensitive-semantic-versioning") version "0.3.0"
        id("org.danilopianini.publish-on-central") version "0.5.0"
    }
}

rootProject.name = "Template-for-Gradle-Plugins"
