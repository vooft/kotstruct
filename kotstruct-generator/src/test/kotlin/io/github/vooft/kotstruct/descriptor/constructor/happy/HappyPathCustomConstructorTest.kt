package io.github.vooft.kotstruct.descriptor.constructor.happy

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import io.github.vooft.kotstruct.GENERATED_PACKAGE
import io.github.vooft.kotstruct.GENERATED_PREFIX
import io.github.vooft.kotstruct.KotStructDescriptor
import io.github.vooft.kotstruct.KotStructMapper
import io.github.vooft.kotstruct.KotStructMapperDescriptor
import io.github.vooft.kotstruct.KotStructMapperDslProcessorProvider
import io.github.vooft.kotstruct.descriptor.constructor.happy.HappyPathCustomConstructorTest.Mappers.CustomConstructorMapper
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.reflect.KFunction1

class HappyPathCustomConstructorTest {
    @Test
    fun `should generate class using custom constructor`() {
        val compilation = KotlinCompilation().apply {
            sources = listOf()
            symbolProcessorProviders = listOf(KotStructMapperDslProcessorProvider(HappyPathCustomConstructorTest::class))
            inheritClassPath = true
            messageOutputStream = System.out // see diagnostics in real time
        }
        val result = compilation.compile()

        val generatedFile = compilation.kspSourcesDir.toPath().resolve("kotlin")
            .resolve(Path(".", *GENERATED_PACKAGE.split(".").toTypedArray()))
            .resolve("$GENERATED_PREFIX${CustomConstructorMapper::class.simpleName}.kt")
        generatedFile.shouldExist()
        println(generatedFile.readText())

        result.exitCode shouldBe KotlinCompilation.ExitCode.OK
    }

    @Suppress("unused")
    class Mappers {
        data class SourceDto(val id: String)
        data class TargetDto(val id: String, val name: String) {
            constructor(id: String): this(id = id, name = "this is a default name")
        }

        @KotStructMapperDescriptor(CustomConstructorMapperDescriptor::class)
        interface CustomConstructorMapper : KotStructMapper {
            fun map(src: SourceDto): TargetDto
        }

        object CustomConstructorMapperDescriptor : KotStructDescriptor<TargetDto> {
            override val constructor: KFunction1<String, TargetDto> = ::TargetDto
        }
    }
}
