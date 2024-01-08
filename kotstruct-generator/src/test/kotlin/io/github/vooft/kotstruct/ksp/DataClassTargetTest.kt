package io.github.vooft.kotstruct.ksp

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.shouldBe
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.io.path.readText

class DataClassTargetTest {
    @TestFactory
    fun `should generate when target is data class and source`() = mapOf(
        "is a data class" to SourceFile.fromDto(
            contents = "data class FromDto(val id: String, val name: String, val extraProperty: String)"
        )
    ).dynamicTests {
        val kotlinSource = SourceFile.kotlin(
            "ExampleMapper.kt", """
                import io.github.vooft.kotstruct.KotStructMapper
                data class ToDto(val id: String, val name: String)
                interface ExampleMapper: KotStructMapper<FromDto, ToDto>
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
                import io.github.vooft.kotstruct.KotStructMapper
                data class ToDto(val id: String, val name: String)
                interface ExampleMapper: KotStructMapper<FromDto, ToDto>
            """
        )

        val result = compile(kotlinSource, it)
        result.exitCode shouldBe KotlinCompilation.ExitCode.COMPILATION_ERROR
    }
}

private fun createCompilation(src: SourceFile, vararg miscFiles: SourceFile): KotlinCompilation {
    return KotlinCompilation().apply {
        sources = listOf(src) + miscFiles.toList()
        symbolProcessorProviders = listOf(KotStructMapperProcessorProvider())
        inheritClassPath = true
        messageOutputStream = System.out // see diagnostics in real time
    }
}

private fun compile(src: SourceFile, vararg miscFiles: SourceFile): KotlinCompilation.Result {
    return createCompilation(src, *miscFiles).compile()
}

private fun Map<String, SourceFile>.dynamicTests(body: (SourceFile) -> Unit) = map { (name, fromDtoCode) ->
    dynamicTest(name) { body(fromDtoCode) }
}

private fun SourceFile.Companion.fromDto(@Language("kotlin") contents: String) = kotlin("FromDto.kt", contents)
