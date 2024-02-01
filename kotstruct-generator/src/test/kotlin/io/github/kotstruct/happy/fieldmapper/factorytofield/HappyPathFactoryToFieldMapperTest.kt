package io.github.kotstruct.happy.fieldmapper.factorytofield

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import io.github.kotstruct.GENERATED_PACKAGE
import io.github.kotstruct.GENERATED_PREFIX
import io.github.kotstruct.KotStructDescribedBy
import io.github.kotstruct.KotStructDescriptor
import io.github.kotstruct.KotStructMapper
import io.github.kotstruct.KotStructMapperDslProcessorProvider
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID
import kotlin.io.path.Path
import kotlin.io.path.readText

class HappyPathFactoryToFieldMapperTest {
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
            .resolve("$GENERATED_PREFIX${Mappers.MyMapper::class.simpleName}.kt")
        generatedFile.shouldExist()
        println(generatedFile.readText())
    }

    @Suppress("unused")
    class Mappers {
        data class SourceDto(val nested: Nested) {
            data class Nested(val srcUuid: String)
        }
        data class TargetDto(val instant: Instant, val nested: Nested) {
            data class Nested(val uuid: UUID)
        }

        @KotStructDescribedBy(MyMapperDescriptor::class)
        interface MyMapper : KotStructMapper {
            fun map(src: SourceDto): TargetDto
        }

        object MyMapperDescriptor : KotStructDescriptor by KotStructDescriptor.kotStruct({
            mapperFor<String, UUID> { UUID.fromString(it) }
            mappingFor<SourceDto, TargetDto> {
                mapFactory { UUID.randomUUID() } into { TargetDto::nested / TargetDto.Nested::uuid }
                mapFactory { Instant.now() } into { TargetDto::instant }
            }
        })
    }
}
