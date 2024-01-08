package io.github.vooft.kotstruct.ksp

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspSourcesDir
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.shouldBe
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.TestFactory
import kotlin.io.path.readText

class DataClassTargetOneToOneTest {
    @TestFactory
    fun `should generate when target is data class and source`() = mapOf(
        "is a data class" to SourceFile.fromDto(
            contents = "data class FromDto(val id: String, val name: String, val extraProperty: String)"
        ),
        "is not a data class with calculated fields" to SourceFile.fromDto(
            contents = """
                class FromDto {
                    val id = "id" + java.util.UUID.randomUUID()
                    val name = "name" + java.util.UUID.randomUUID()
                }
            """.trimIndent()
        ),
        "is not a data class and without backing fields" to SourceFile.fromDto(
            contents = """
                class FromDto {
                    val id: String get() = "id" + java.util.UUID.randomUUID()
                    val name: String get() = "name" + java.util.UUID.randomUUID()
                }
            """.trimIndent()
        ),
        "is not a data class with lateinit var fields" to SourceFile.fromDto(
            contents = """
                class FromDto {
                    lateinit var id: String
                    lateinit var name: String
                }
            """.trimIndent()
        ),
    ).dynamicTests {
        val kotlinSource = SourceFile.kotlin(
            "ExampleMapper.kt", """
                data class ToDto(val id: String, val name: String)
                interface ExampleMapper: io.github.vooft.kotstruct.KotStructMapper<FromDto, ToDto>
            """
        )

        val compilation = createCompilation(kotlinSource, it)
        val result = compilation.compile()

        val generatedFile = compilation.kspSourcesDir.toPath().resolve("kotlin").resolve("${GENERATED_PREFIX}ExampleMapper.kt")
        generatedFile.shouldExist()
        println(generatedFile.readText())

        result.exitCode shouldBe KotlinCompilation.ExitCode.OK
    }

    @TestFactory
    fun `should fail to generate when target is data class and source`() = mapOf(
        "does not have enough fields" to SourceFile.fromDto(
            contents = "data class FromDto(val id: String)"
        ),
        "does not have matching fields" to SourceFile.fromDto(
            contents = "data class FromDto(val id: String, val name1: String)"
        )
    ).dynamicTests {
        val kotlinSource = SourceFile.kotlin(
            "ExampleMapper.kt", """
                data class ToDto(val id: String, val name: String)
                interface ExampleMapper: io.github.vooft.kotstruct.KotStructMapper<FromDto, ToDto>
            """
        )

        val result = compile(kotlinSource, it)
        result.exitCode shouldBe KotlinCompilation.ExitCode.COMPILATION_ERROR
    }
}

private fun SourceFile.Companion.fromDto(@Language("kotlin") contents: String) = kotlin("FromDto.kt", contents)
