package io.github.vooft.kotstruct.dataclass.onetoone.happy

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import io.github.vooft.kotstruct.GENERATED_PACKAGE
import io.github.vooft.kotstruct.GENERATED_PREFIX
import io.github.vooft.kotstruct.KotStructMapper
import io.github.vooft.kotstruct.KotStructMapperDslProcessorProvider
import io.github.vooft.kotstruct.dynamicTests
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
        "is a data class" to SimpleDataClassMapper::class,
        "is not a data class with calculated fields" to RegularClassCalculatedFieldsMapper::class,
        "is not a data class and without backing fields" to RegularClassWithoutBackingFieldsMapper::class,
        "is not a data class with lateinit var fields" to RegularClassWithLateinitFieldsMapper::class,
    ) {
        val compilation = KotlinCompilation().apply {
            sources = listOf()
            symbolProcessorProviders = listOf(KotStructMapperDslProcessorProvider(HappyPathDataClassTargetOneToOneTest::class))
            inheritClassPath = true
            messageOutputStream = System.out // see diagnostics in real time
        }
        val result = compilation.compile()

        val generatedFile = compilation.kspSourcesDir.toPath().resolve("kotlin")
            .resolve(Path(".", *GENERATED_PACKAGE.split(".").toTypedArray()))
            .resolve("$GENERATED_PREFIX${it.simpleName}.kt")
        generatedFile.shouldExist()
        println(generatedFile.readText())

        result.exitCode shouldBe KotlinCompilation.ExitCode.OK
    }

    data class SimpleDataClassSourceDto(val id: String, val name: String, val extraProperty: String)
    interface SimpleDataClassMapper : KotStructMapper<SimpleDataClassSourceDto, TestTargetDto>

    class RegularClassCalculatedFieldsSourceDto {
        val id = "id" + UUID.randomUUID()
        val name = "name" + UUID.randomUUID()
    }
    interface RegularClassCalculatedFieldsMapper : KotStructMapper<RegularClassCalculatedFieldsSourceDto, TestTargetDto>

    class RegularClassWithoutBackingFieldsSourceDto {
        val id: String get() = "id" + UUID.randomUUID()
        val name: String get() = "name" + UUID.randomUUID()
    }
    interface RegularClassWithoutBackingFieldsMapper : KotStructMapper<RegularClassWithoutBackingFieldsSourceDto, TestTargetDto>

    class RegularClassWithLateinitFieldsSourceDto {
        lateinit var id: String
        lateinit var name: String
    }

    interface RegularClassWithLateinitFieldsMapper : KotStructMapper<RegularClassWithLateinitFieldsSourceDto, TestTargetDto>
}
