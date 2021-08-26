plugins {
    id("de.fayard.refreshVersions") version "0.20.0"
}
refreshVersions {
    featureFlags {
        enable(de.fayard.refreshVersions.core.FeatureFlag.LIBS)
    }
}

rootProject.name = "Template-for-Gradle-Plugins"
