package org.danilopianini.gradle.test

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.yaml
import io.github.classgraph.ClassGraph
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.file.shouldBeAFile
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.io.File
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Tests :
    StringSpec(
        {
            val testkitProperties = javaClass.classLoader.getResource("testkit-gradle.properties")?.readText()
            checkNotNull(testkitProperties) {
                "No file testkit-gradle.properties was generated"
            }
            val scan =
                ClassGraph()
                    .enableAllInfo()
                    .acceptPackages(Tests::class.java.`package`.name)
                    .scan()
            scan
                .getResourcesWithLeafName("test.yaml")
                .flatMap { resource ->
                    log.debug("Found test list in {}", resource)
                    val yamlFile = File(resource.classpathElementFile.absolutePath + "/" + resource.path)
                    val testConfiguration =
                        Config {
                            addSpec(Root)
                        }.from.yaml.inputStream(resource.open())
                    testConfiguration[Root.tests].map { it to yamlFile.parentFile }
                }.forEach { (test, location) ->
                    log.debug("Test to be executed: {} from {}", test, location)
                    val testFolder =
                        folder {
                            location.copyRecursively(this.root)
                        }
                    log.debug("Test has been copied into {} and is ready to get executed", testFolder)
                    test.description {
                        File(testFolder.root, "gradle.properties").writeText(testkitProperties)
                        val result =
                            GradleRunner
                                .create()
                                .withProjectDir(testFolder.root)
                                .withPluginClasspath()
                                .withArguments(test.configuration.tasks + test.configuration.options)
                                .run { if (test.expectation.failure.isEmpty()) build() else buildAndFail() }
                        println(result.tasks)
                        println(result.output)
                        test.expectation.output_contains.forEach {
                            result.output shouldContain it
                        }
                        test.expectation.output_doesnt_contain.forEach {
                            result.output shouldNotContain it
                        }
                        test.expectation.success.forEach {
                            result.outcomeOf(it) shouldBe TaskOutcome.SUCCESS
                        }
                        test.expectation.failure.forEach {
                            result.outcomeOf(it) shouldBe TaskOutcome.FAILED
                        }
                        test.expectation.file_exists.forEach {
                            val file =
                                File("${testFolder.root.absolutePath}/${it.name}").apply {
                                    shouldExist()
                                    shouldBeAFile()
                                }
                            it.validate(file)
                        }
                    }
                }
        },
    ) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(Tests::class.java)

        private fun BuildResult.outcomeOf(name: String) = checkNotNull(task(":$name")?.outcome) {
            "Task $name was not present among the executed tasks"
        }

        private fun folder(closure: TemporaryFolder.() -> Unit) = TemporaryFolder().apply {
            create()
            closure()
        }
    }
}
