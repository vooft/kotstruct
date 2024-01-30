package io.github.kotstruct.happy.onetoone

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import io.github.kotstruct.GENERATED_PACKAGE
import io.github.kotstruct.GENERATED_PREFIX
import io.github.kotstruct.KotStructMapper
import io.github.kotstruct.KotStructMapperDslProcessorProvider
import io.github.kotstruct.dynamicTests
import io.github.kotstruct.happy.onetoone.HappyPathDataClassTargetOneToOneTest.Mappers.RegularClassCalculatedFieldsMapper
import io.github.kotstruct.happy.onetoone.HappyPathDataClassTargetOneToOneTest.Mappers.RegularClassWithLateinitFieldsMapper
import io.github.kotstruct.happy.onetoone.HappyPathDataClassTargetOneToOneTest.Mappers.RegularClassWithoutBackingFieldsMapper
import io.github.kotstruct.happy.onetoone.HappyPathDataClassTargetOneToOneTest.Mappers.SimpleDataClassMapper
import io.github.kotstruct.happy.onetoone.HappyPathDataClassTargetOneToOneTest.Mappers.SourceParameterNameDifferentMapper
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.TestFactory
import java.util.UUID
import kotlin.io.path.Path
import kotlin.io.path.readText

class HappyPathDataClassTargetOneToOneTest {
    data class TestTargetDto(val id: String, val name: String)

    @TestFactory
    fun `should generate when target is data class and source`() = dynamicTests(
        "argument is not named src" to SourceParameterNameDifferentMapper::class,
        "is a data class" to SimpleDataClassMapper::class,
        "is not a data class with calculated fields" to RegularClassCalculatedFieldsMapper::class,
        "is not a data class and without backing fields" to RegularClassWithoutBackingFieldsMapper::class,
        "is not a data class with lateinit var fields" to RegularClassWithLateinitFieldsMapper::class,
    ) { mapperClass ->
        val compilation = KotlinCompilation().also {
            it.sources = listOf()
            it.symbolProcessorProviders = listOf(KotStructMapperDslProcessorProvider(this::class))
            it.inheritClassPath = true
            it.messageOutputStream = System.out // see diagnostics in real time
        }
        val result = compilation.compile()

        val generatedFile = compilation.kspSourcesDir.toPath().resolve("kotlin")
            .resolve(Path(".", *GENERATED_PACKAGE.split(".").toTypedArray()))
            .resolve("$GENERATED_PREFIX${mapperClass.simpleName}.kt")
        generatedFile.shouldExist()
        println(generatedFile.readText())

        result.exitCode shouldBe KotlinCompilation.ExitCode.OK
    }

    @Suppress("unused")
    class Mappers {
        data class SimpleDataClassSourceDto(val id: String, val name: String, val extraProperty: String)
        interface SimpleDataClassMapper : KotStructMapper {
            fun map(src: SimpleDataClassSourceDto): TestTargetDto
        }

        class RegularClassCalculatedFieldsSourceDto {
            val id = "id" + UUID.randomUUID()
            val name = "name" + UUID.randomUUID()
        }
        interface RegularClassCalculatedFieldsMapper : KotStructMapper {
            fun map(src: RegularClassCalculatedFieldsSourceDto): TestTargetDto
        }

        class RegularClassWithoutBackingFieldsSourceDto {
            val id: String get() = "id" + UUID.randomUUID()
            val name: String get() = "name" + UUID.randomUUID()
        }
        interface RegularClassWithoutBackingFieldsMapper : KotStructMapper {
            fun map(src: RegularClassWithoutBackingFieldsSourceDto): TestTargetDto
        }

        class RegularClassWithLateinitFieldsSourceDto {
            lateinit var id: String
            lateinit var name: String
        }

        interface RegularClassWithLateinitFieldsMapper : KotStructMapper {
            fun map(src: RegularClassWithLateinitFieldsSourceDto): TestTargetDto
        }

        interface SourceParameterNameDifferentMapper : KotStructMapper {
            fun map(mySuperCustomArgumentName: SimpleDataClassSourceDto): TestTargetDto
        }
    }
}
