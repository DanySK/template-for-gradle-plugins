plugins {
    jacoco
    `java-gradle-plugin`
    kotlin("jvm")
    `maven-publish`
    signing
    id("com.gradle.plugin-publish")
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.dokka")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.danilopianini.git-sensitive-semantic-versioning")
    id("org.danilopianini.publish-on-central")
}

group = "org.danilopianini"

gitSemVer {
    version = computeGitSemVer()
}

repositories {
    mavenCentral()
    mapOf(
        "kotlin/dokka" to setOf("org.jetbrains.dokka"),
        "kotlin/kotlinx.html" to setOf("org.jetbrains.kotlinx"),
        "arturbosch/code-analysis" to setOf("io.gitlab.arturbosch.detekt")
    ).forEach { (uriPart, groups) ->
        maven {
            url = uri("https://dl.bintray.com/$uriPart")
            content { groups.forEach { includeGroup(it) } }
        }
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
tasks.withType<Test> { dependsOn(createClasspathManifest) }

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:_")
    api(gradleApi())
    api(gradleKotlinDsl())
    implementation(kotlin("stdlib"))
    testImplementation(gradleTestKit())
    testImplementation("io.kotest:kotest-runner-junit5:_")
    testImplementation("io.kotest:kotest-assertions-core-jvm:_")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:_")
    testImplementation("org.mockito:mockito-core:_")
    testRuntimeOnly(files(createClasspathManifest))
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

tasks.test {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        showCauses = true
        showStackTraces = true
        events(*org.gradle.api.tasks.testing.logging.TestLogEvent.values())
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.jacocoTestReport {
    reports {
        // Used by Codecov.io
        xml.isEnabled = true
    }
}

detekt {
    failFast = true
    buildUponDefaultConfig = true
    config = files("$projectDir/config/detekt.yml")
    reports {
        html.enabled = true
        xml.enabled = true
        txt.enabled = true
    }
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTask> {
    // Workaround for https://github.com/Kotlin/dokka/issues/294
    outputFormat = if (JavaVersion.current().isJava10Compatible) "html" else "javadoc"
    outputDirectory = "$buildDir/javadoc"
    tasks.withType<org.danilopianini.gradle.mavencentral.JavadocJar> {
        from(outputDirectory)
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
group = "org.danilopianini"
description = "A template repository for kickstarting Gradle Plugins"
object ProjectInfo {
    const val longName = "Template for Gradle Plugins"
    const val website = "https://github.com/DanySK/Template-for-Gradle-Plugins"
    const val scm = "git@github.com:DanySK/Template-for-Gradle-Plugins.git"
    val pluginImplementationClass = "$group.template.HelloGradle"
    val tags = listOf("template", "kickstart", "example")
}

publishOnCentral {
    projectLongName.set(ProjectInfo.longName)
    projectDescription.set(description)
    projectUrl.set(ProjectInfo.website)
    scmConnection.set(ProjectInfo.scm)
//    licenseName.set("...") // Defaults to Apache 2.0
//    licenseUrl.set("...") // Defaults to Apache 2.0 url
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
    website = ProjectInfo.website
    vcsUrl = ProjectInfo.website
    tags = ProjectInfo.tags
}

gradlePlugin {
    plugins {
        create("GradleLatex") {
            id = "$group.${project.name}"
            displayName = ProjectInfo.longName
            description = project.description
            implementationClass = ProjectInfo.pluginImplementationClass
        }
    }
}
