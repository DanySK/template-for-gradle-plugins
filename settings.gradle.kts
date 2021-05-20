import de.fayard.refreshVersions.bootstrapRefreshVersions
import org.danilopianini.VersionAliases.justAdditionalAliases
buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    dependencies {
        classpath("de.fayard.refreshVersions:refreshVersions:0.10.0")
        classpath("org.danilopianini:refreshversions-aliases:+")
    }
}
bootstrapRefreshVersions(justAdditionalAliases)
rootProject.name = "Template-for-Gradle-Plugins"
