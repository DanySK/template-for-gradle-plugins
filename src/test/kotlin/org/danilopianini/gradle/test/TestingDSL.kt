package org.danilopianini.gradle.test

import com.uchuhimo.konf.ConfigSpec
import java.io.File
import org.gradle.internal.impldep.org.apache.commons.lang.StringUtils

object Root : ConfigSpec("") {
    val tests by required<List<Test>>()
}

data class Test(
    val description: String,
    val configuration: Configuration,
    val expectation: Expectation,
)

data class Configuration(
    val tasks: List<String>,
    val options: List<String> = emptyList(),
)

@Suppress("ConstructorParameterNaming")
data class Expectation(
    val file_exists: List<ExistingFile> = emptyList(),
    val success: List<String> = emptyList(),
    val failure: List<String> = emptyList(),
    val output_contains: List<String> = emptyList(),
    val output_doesnt_contain: List<String> = emptyList(),
)

enum class Permission(
    private val hasPermission: File.() -> Boolean,
) {
    R(File::canRead),
    W(File::canWrite),
    X(File::canExecute),
    ;

    fun requireOnFile(file: File) = require(file.hasPermission()) {
        "File ${file.absolutePath} must have permission $name, but it does not."
    }
}

data class ExistingFile(
    val name: String,
    val findRegex: List<String> = emptyList(),
    val content: String? = null,
    val trim: Boolean = false,
    val permissions: List<Permission> = emptyList(),
) {
    fun validate(actualFile: File): Unit = with(actualFile) {
        require(exists()) {
            "File $name does not exist."
        }
        if (content != null) {
            val text = readText()
            require(text == content) {
                """
                    Content of $name does not match expectations.
                    
                    Expected:
                    $content
                    
                    Actual:
                    $text
                    
                    Difference starts at index ${StringUtils.indexOfDifference(content, text)}:
                    ${StringUtils.difference(content, text)}
                """.trimIndent()
            }
        }
        findRegex.forEach { regexString ->
            val regex = Regex(regexString)
            requireNotNull(readLines().find { regex.matches(it) }) {
                """
                    None of the lines in $name matches the regular expression $findRegex. File content:
                    ${readText()}
                """.trimIndent()
            }
        }
        permissions.forEach { it.requireOnFile(this) }
    }
}
