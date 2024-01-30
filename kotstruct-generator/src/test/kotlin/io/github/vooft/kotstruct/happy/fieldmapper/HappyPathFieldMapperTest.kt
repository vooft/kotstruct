package io.github.vooft.kotstruct.happy.fieldmapper

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import io.github.vooft.kotstruct.FieldMapping
import io.github.vooft.kotstruct.GENERATED_PACKAGE
import io.github.vooft.kotstruct.GENERATED_PREFIX
import io.github.vooft.kotstruct.KotStructDescribedBy
import io.github.vooft.kotstruct.KotStructDescriptor
import io.github.vooft.kotstruct.KotStructMapper
import io.github.vooft.kotstruct.KotStructMapperDslProcessorProvider
import io.github.vooft.kotstruct.MappingsDefinitions
import io.github.vooft.kotstruct.happy.fieldmapper.HappyPathFieldMapperTest.Mappers.MyMapper
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import kotlin.io.path.Path
import kotlin.io.path.readText

class HappyPathFieldMapperTest {
    @Test
    fun `should generate class with field mappings`() {
        val compilation = KotlinCompilation().also {
            it.sources = listOf()
            it.symbolProcessorProviders = listOf(KotStructMapperDslProcessorProvider(this::class))
            it.inheritClassPath = true
            it.messageOutputStream = System.out // see diagnostics in real time
        }
        val result = compilation.compile()
        result.exitCode shouldBe KotlinCompilation.ExitCode.OK

        val generatedFile = compilation.kspSourcesDir.toPath().resolve("kotlin")
            .resolve(Path(".", *GENERATED_PACKAGE.split(".").toTypedArray()))
            .resolve("$GENERATED_PREFIX${MyMapper::class.simpleName}.kt")
        generatedFile.shouldExist()
        println(generatedFile.readText())
    }

    @Suppress("unused")
    class Mappers {
        data class SourceDto(val srcId: String, val nested: Nested) {
            data class Nested(val srcName: String)
        }
        data class TargetDto(val id: String, val nested: Nested) {
            data class Nested(val name: String)
        }

        @KotStructDescribedBy(MyMapperDescriptor::class)
        interface MyMapper : KotStructMapper {
            fun map(src: SourceDto): TargetDto
        }

        object MyMapperDescriptor : KotStructDescriptor {
            override val mappings = MappingsDefinitions(
                fieldMappings = listOf(
                    FieldMapping.create<SourceDto, TargetDto>(listOf(SourceDto::srcId), listOf(TargetDto::id)),
                    FieldMapping.create<SourceDto, TargetDto>(
                        fromPath = listOf(SourceDto::nested, SourceDto.Nested::srcName),
                        toPath = listOf(TargetDto::nested, TargetDto.Nested::name)
                    )
                )
            )
        }
    }
}

