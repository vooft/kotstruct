package io.github.vooft.kotstruct.dataclass.onetoone.unhappy

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.symbolProcessorProviders
import io.github.vooft.kotstruct.KotStructMapper
import io.github.vooft.kotstruct.KotStructMapperDslProcessorProvider
import io.github.vooft.kotstruct.dataclass.onetoone.unhappy.UnhappyPathDataClassTargetOneToOneTest.Mappers.NoMatchingFieldsMapper
import io.github.vooft.kotstruct.dataclass.onetoone.unhappy.UnhappyPathDataClassTargetOneToOneTest.Mappers.NotEnoughFieldsMapper
import io.github.vooft.kotstruct.dynamicTests
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.TestFactory

class UnhappyPathDataClassTargetOneToOneTest {
    data class TestTargetDto(val id: String, val name: String)

    @TestFactory
    fun `should fail to generate when target is data class and source`() = dynamicTests(
        "does not have enough fields" to NotEnoughFieldsMapper::class,
        "does not have matching fields" to NoMatchingFieldsMapper::class
    ) {
        val compilation = KotlinCompilation().also {
            it.sources = listOf()
            it.symbolProcessorProviders = listOf(KotStructMapperDslProcessorProvider(this::class))
            it.inheritClassPath = true
            it.messageOutputStream = System.out // see diagnostics in real time
        }
        val result = compilation.compile()
        result.exitCode shouldBe KotlinCompilation.ExitCode.COMPILATION_ERROR
    }

    @Suppress("unused")
    class Mappers {
        data class NotEnoughFieldsSourceDto(val id: String)
        interface NotEnoughFieldsMapper : KotStructMapper {
            fun map(src: NotEnoughFieldsSourceDto): TestTargetDto
        }

        data class NoMatchingFieldsSourceDto(val id: String, val name1: String)
        interface NoMatchingFieldsMapper : KotStructMapper {
            fun map(src: NoMatchingFieldsSourceDto): TestTargetDto
        }
    }
}
