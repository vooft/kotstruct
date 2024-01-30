package io.github.vooft.kotstruct.happy.nested.custommapper

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import io.github.vooft.kotstruct.GENERATED_PACKAGE
import io.github.vooft.kotstruct.GENERATED_PREFIX
import io.github.vooft.kotstruct.KotStructDescribedBy
import io.github.vooft.kotstruct.KotStructDescriptor
import io.github.vooft.kotstruct.KotStructMapper
import io.github.vooft.kotstruct.KotStructMapperDslProcessorProvider
import io.github.vooft.kotstruct.MappingsDefinitions
import io.github.vooft.kotstruct.TypeMapping
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.io.path.Path
import kotlin.io.path.readText

class HappyPathNestedCustomMapperTest {
    @Test
    fun `should generate class using custom mapper`() {
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
            .resolve("$GENERATED_PREFIX${Mappers.NestedCustomMapperMapper::class.simpleName}.kt")
        generatedFile.shouldExist()
        println(generatedFile.readText())
    }

    @Suppress("unused")
    class Mappers {
        data class SourceDto(val id: String, val uuidToString: UUID, val stringToUUID: String)
        data class TargetDto(val id: String, val uuidToString: String, val stringToUUID: UUID)

        @KotStructDescribedBy(NestedCustomMapperMapperDescriptor::class)
        interface NestedCustomMapperMapper : KotStructMapper {
            fun map(src: SourceDto): TargetDto
        }

        object NestedCustomMapperMapperDescriptor : KotStructDescriptor {
            override val mappings = MappingsDefinitions(
                typeMappings = listOf(
                    TypeMapping.create<String, UUID> { UUID.fromString(it) },
                    TypeMapping.create<UUID, String> { it.toString() },
                )
            )
        }
    }
}
