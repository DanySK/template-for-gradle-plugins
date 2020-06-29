plugins {
    kotlin("jvm")
    jacoco
    `maven-publish`
    signing
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

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:_")
    implementation(kotlin("stdlib"))
    testImplementation("io.kotest:kotest-runner-junit5:_")
    testImplementation("io.kotest:kotest-assertions-core-jvm:_")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:_")
    testImplementation("org.mockito:mockito-core:_")
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

publishOnCentral {
    projectLongName.set("Template Kotlin JVM Project")
    projectDescription.set("A template repository for Kotlin JVM projects")
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
