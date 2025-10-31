package org.danilopianini.template

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.register

/**
 * Just a template.
 */
open class HelloGradle : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create<HelloExtension>("hello")
        // Enables `hello { ... }` in build.gradle.kts
        target.tasks.register<HelloTask>("hello") {
            author.set(extension.author)
        }
    }
}

/**
 * Just a template.
 */
open class HelloTask : DefaultTask() {
    /**
     * The author of the greeting, lazily set.
     */
    @get:Input
    val author: Property<String> = project.objects.property()

    /**
     * Read-only property calculated from the greeting.
     */
    @get:Internal
    val message: Provider<String> = author.map { "Hello from $it" }

    /**
     * This is the code that is executed when the task is run.
     */
    @TaskAction
    fun printMessage() {
        logger.quiet(message.get())
    }
}

/**
 * Just a template.
 */
open class HelloExtension(objects: ObjectFactory) {
    /**
     * This is where you write your DSL to control the plugin.
     */
    val author: Property<String> = objects.property()
}
