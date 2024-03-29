package io.github.kotstruct.happy.nested.onetoone

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import io.github.kotstruct.GENERATED_PACKAGE
import io.github.kotstruct.GENERATED_PREFIX
import io.github.kotstruct.KotStructDescribedBy
import io.github.kotstruct.KotStructDescriptor
import io.github.kotstruct.KotStructDescriptor.Companion.kotStruct
import io.github.kotstruct.KotStructMapper
import io.github.kotstruct.KotStructMapperDslProcessorProvider
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.io.path.Path
import kotlin.io.path.readText

class NestedOneToOneMapperTest {
    @Test
    fun `should generate class with nested classes`() {
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
        data class SourceDto(val id: IdWrapper) {
            data class IdWrapper(val id: ValueWrapper) {
                data class ValueWrapper(val value: String)
            }
        }
        data class TargetDto(val id: IdWrapper) {
            data class IdWrapper(val id: ValueWrapper) {
                data class ValueWrapper(val value: String)
            }
        }

        @KotStructDescribedBy(MyMapperDescriptor::class)
        interface MyMapper : KotStructMapper {
            fun map(src: SourceDto): TargetDto
        }

        object MyMapperDescriptor : KotStructDescriptor by kotStruct({
            mapperFor<String, UUID> { UUID.fromString(it) }
            mapperFor<UUID, String> { it.toString() }
        })
    }
}

