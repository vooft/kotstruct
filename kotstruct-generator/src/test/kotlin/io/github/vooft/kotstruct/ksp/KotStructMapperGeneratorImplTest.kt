package io.github.vooft.kotstruct.ksp

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import kotlin.io.path.readText

class KotStructMapperGeneratorImplTest {
    private val kotlinSource = SourceFile.kotlin(
        "ExampleMapper.kt", """
                import io.github.vooft.kotstruct.KotStructMapper
                
                data class FromDto(val id: String, val name: String, val extraProperty: String)
                data class ToDto(val id: String, val name: String)
                
                interface ExampleMapper: KotStructMapper<FromDto, ToDto>
            """
    )

    @Test
    fun test() {
        val compilation = KotlinCompilation().apply {
            sources = listOf(kotlinSource)
            symbolProcessorProviders = listOf(KotStructMapperProcessorProvider())
            inheritClassPath = true
            messageOutputStream = System.out // see diagnostics in real time
        }

        val result = compilation.compile()

        val file = compilation.kspSourcesDir.toPath().resolve("kotlin").resolve("${GENERATED_PREFIX}ExampleMapper.kt")
        println(file.readText())

        result.exitCode shouldBe KotlinCompilation.ExitCode.OK
    }
}
