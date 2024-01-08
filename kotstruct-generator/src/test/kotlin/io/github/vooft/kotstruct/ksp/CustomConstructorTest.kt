package io.github.vooft.kotstruct.ksp

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspSourcesDir
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import kotlin.io.path.readText

class CustomConstructorTest {
    @Test
    fun `should use custom constructor`() {
        val kotlinSource = SourceFile.kotlin(
            "ExampleMapper.kt", """
                data class FromDto(val id: String)
                data class ToDto(val id: String, val name: String) {
                    constructor(id: String): this(id = id, name = "name")
                }
                interface ExampleMapper: io.github.vooft.kotstruct.KotStructMapper<FromDto, ToDto> {
                    @io.github.vooft.kotstruct.KotStructCustomConstructor
                    fun myConstructor(id: String) = ToDto(id)
                }
            """
        )

        val compilation = createCompilation(kotlinSource)
        val result = compilation.compile()

        val generatedFile = compilation.kspSourcesDir.toPath().resolve("kotlin").resolve("${GENERATED_PREFIX}ExampleMapper.kt")
        generatedFile.shouldExist()
        println(generatedFile.readText())

        result.exitCode shouldBe KotlinCompilation.ExitCode.OK
    }
}
