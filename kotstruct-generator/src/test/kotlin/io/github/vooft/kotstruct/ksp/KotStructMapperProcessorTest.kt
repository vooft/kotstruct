package io.github.vooft.kotstruct.ksp

/*import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Test

class KotStructMapperProcessorTest {
    private val kotlinSource = SourceFile.kotlin(
        "ExampleMapper.kt", """
                import io.github.vooft.kotstruct.KotStructMapper
                
                data class FromDto(val id: String, val name: String)
                data class ToDto(val id: String, val name: String)
                
                interface ExampleMapper: KotStructMapper<FromDto, ToDto>
            """
    )

    private val generatorSlot = slot<KSClassDeclaration>()
    private val generator = mockk<KotStructMapperGenerator> {
        every { generateFor(capture(generatorSlot)) } returns Unit
    }

    @Test
    fun `should invoke generator with discovered class`() {
        val result = KotlinCompilation().apply {
            sources = listOf(kotlinSource)
            symbolProcessorProviders = listOf(KotStructMapperProcessorProvider(generator))
            inheritClassPath = true
            messageOutputStream = System.out // see diagnostics in real time
        }.compile()

        println(result)

        val captured = generatorSlot.captured
        captured.simpleName.asString() shouldBe "ExampleMapper"

        result.exitCode shouldBe KotlinCompilation.ExitCode.OK
    }
}*/
