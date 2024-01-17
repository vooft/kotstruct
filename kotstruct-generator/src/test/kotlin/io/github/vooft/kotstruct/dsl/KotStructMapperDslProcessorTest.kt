package io.github.vooft.kotstruct.dsl

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.symbolProcessorProviders
import io.github.vooft.kotstruct.KotStructMapper
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class KotStructMapperDslProcessorTest {
    @Test
    fun `should invoke generator with discovered class`() {
        val result = KotlinCompilation().apply {
            sources = listOf()
            symbolProcessorProviders = listOf(KotStructMapperDslProcessorProvider(KotStructMapperDslProcessorTest::class.java.packageName))
            inheritClassPath = true
            messageOutputStream = System.out // see diagnostics in real time
        }.compile()

        println(result)

        result.exitCode shouldBe KotlinCompilation.ExitCode.OK
    }
}

data class FromDto(val id: String, val name: String)
data class ToDto(val id: String, val name: String)

object ExampleMapper: KotStructMapper<FromDto, ToDto>
