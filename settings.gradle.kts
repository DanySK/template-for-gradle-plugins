import org.danilopianini.VersionAliases.justAdditionalAliases
buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    dependencies {
        classpath("org.danilopianini:refreshversions-aliases:+")
    }
}

plugins {
    id("de.fayard.refreshVersions") version "0.10.0"
}

refreshVersions {
    extraArtifactVersionKeyRules = justAdditionalAliases
}

rootProject.name = "Template-for-Gradle-Plugins"
