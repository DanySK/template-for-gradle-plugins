import java.net.URL
import org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION as KOTLIN_VERSION

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    jacoco
    `java-gradle-plugin`
    alias(libs.plugins.dokka)
    alias(libs.plugins.gitSemVer)
    alias(libs.plugins.gradlePluginPublish)
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.publishOnCentral)
}

/*
 * Project information
 */
group = "org.danilopianini"
description = "A template repository for kickstarting Gradle Plugins"
inner class ProjectInfo {
    val longName = "Template for Gradle Plugins"
    val website = "https://github.com/DanySK/Template-for-Gradle-Plugins"
    val scm = "git@github.com:DanySK/Template-for-Gradle-Plugins.git"
    val pluginImplementationClass = "$group.template.HelloGradle"
    val tags = listOf("template", "kickstart", "example")
}
val info = ProjectInfo()

gitSemVer {
    buildMetadataSeparator.set("-")
}

repositories {
    mavenCentral()
}

val minimumJava = JavaLanguageVersion.of(8)
val maximumJava = latestSupportedJava()

java {
    toolchain {
        languageVersion.set(minimumJava)
        vendor.set(JvmVendorSpec.ADOPTOPENJDK)
    }
}

/*
 * By default, Gradle does not include all the plugin classpath into the testing classpath.
 * This task creates a descriptor of the runtime classpath, to be injected (manually) when running tests.
 */
val createClasspathManifest = tasks.register("createClasspathManifest") {
    val outputDir = file("$buildDir/$name")
    inputs.files(sourceSets.main.get().runtimeClasspath)
    outputs.dir(outputDir)
    doLast {
        outputDir.mkdirs()
        file("$outputDir/plugin-classpath.txt").writeText(sourceSets.main.get().runtimeClasspath.joinToString("\n"))
    }
}

tasks.withType<Test> {
    dependsOn(createClasspathManifest)
}

fun latestSupportedJava(): JavaLanguageVersion =
    Regex("""<tr.*>[\s|\n|\r]*<td.*>.*?(\d+).*?<\/td>[\s|\n|\r]*<td.*>.*?(\d+(?:\.\d+)).*?<\/td>""")
        .findAll(URL("https://docs.gradle.org/current/userguide/compatibility.html").readText())
        .map {
            val (javaVersion, gradleVersion) = it.destructured
            JavaLanguageVersion.of(javaVersion) to GradleVersion.version(gradleVersion)
        }
        .filter { (_, gradleVersion) -> GradleVersion.current() >= gradleVersion }
        .maxByOrNull { (_, gradleVersion) -> gradleVersion }
        ?.first
        ?: JavaLanguageVersion.of(16)

java {
    toolchain {
        languageVersion.set(minimumJava)
    }
}

val additionalTools: Configuration by configurations.creating

dependencies {
    api(gradleApi())
    api(gradleKotlinDsl())
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(gradleTestKit())
    testImplementation(libs.konf.yaml)
    testImplementation(libs.classgraph)
    testImplementation(libs.bundles.kotlin.testing)
    testRuntimeOnly(files(createClasspathManifest))
}

// Enforce Kotlin version coherence
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin" && requested.name.startsWith("kotlin")) {
            useVersion(KOTLIN_VERSION)
            because("All Kotlin modules should use the same version, and compiler uses $KOTLIN_VERSION")
        }
    }
}

operator fun JavaLanguageVersion.rangeTo(that: JavaLanguageVersion): List<JavaLanguageVersion> =
    (asInt()..that.asInt()).map(JavaLanguageVersion::of)

val JavaLanguageVersion.isLTS: Boolean get() = asInt() == 8 || (asInt() - 11) % 6 == 0

val JvmImplementation.name: String get() = when (this) {
    JvmImplementation.VENDOR_SPECIFIC -> "Classic"
    else -> toString()
}

val testMultiPlatform by tasks.registering
val testWithAllCompatibleJavaVersions by tasks.registering
for (javaVersion in JavaLanguageVersion.of(minimumJava.asInt() + 1)..maximumJava) {
    val testTask = tasks.register<Test>("testWithJava${javaVersion.asInt()}") {
        javaLauncher.set(
            javaToolchains.launcherFor {
                languageVersion.set(javaVersion)
            }
        )
    }
    if (javaVersion.isLTS || javaVersion == maximumJava) {
        testMultiPlatform.configure { dependsOn(testTask) }
    }
    testWithAllCompatibleJavaVersions.configure { dependsOn(testTask) }
}
tasks.check.configure {
    dependsOn(testMultiPlatform)
}

kotlin {
    target {
        compilations.all {
            kotlinOptions {
                allWarningsAsErrors = true
                freeCompilerArgs = listOf("-XXLanguage:+InlineClasses", "-Xopt-in=kotlin.RequiresOptIn")
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        showCauses = true
        showStackTraces = true
        events(*org.gradle.api.tasks.testing.logging.TestLogEvent.values())
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

jacoco {
    toolVersion = libs.versions.jacoco.getOrElse(toolVersion)
}

tasks.jacocoTestReport {
    reports {
        // Used by Codecov.io
        xml.required.set(true)
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
}

/*
 * Publication on Maven Central and the Plugin portal
 */
publishOnCentral {
    projectLongName = info.longName
    projectDescription = description ?: TODO("Missing description")
    projectUrl = info.website
    scmConnection = info.scm
//    licenseName = "..." // Defaults to Apache 2.0
//    licenseUrl = "..." // Defaults to Apache 2.0 url
}

publishing {
    publications {
        withType<MavenPublication> {
            pom {
                developers {
                    developer {
                        name.set("Danilo Pianini")
                        email.set("danilo.pianini@gmail.com")
                        url.set("http://www.danilopianini.org/")
                    }
                }
            }
        }
    }
}

pluginBundle {
    website = info.website
    vcsUrl = info.website
    tags = info.tags
}

gradlePlugin {
    plugins {
        create("GradleLatex") {
            id = "$group.${project.name}"
            displayName = info.longName
            description = project.description
            implementationClass = info.pluginImplementationClass
        }
    }
}
