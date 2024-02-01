package io.github.kotstruct.descriptor

import io.github.kotstruct.KotStructGeneratedFactoryToFieldMappingMapper
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class FactoryToFieldToFieldMappingMapperTest {
    @Test
    fun `should use field mapping`() {
        val mapper = KotStructGeneratedFactoryToFieldMappingMapper()

        val from = FactoryToFieldMappingMapper.SourceDto(
            name = UUID.randomUUID().toString(),
            nested = FactoryToFieldMappingMapper.SourceDto.Nested(
                customDate = Instant.now()
            )
        )

        val actual = mapper.map(from)
        actual.id shouldNotBe UUID.randomUUID()
        actual.name shouldBe from.name
        actual.nested.customDate shouldBe from.nested.customDate
        actual.nested.createdAt shouldNotBe Instant.now().plusSeconds(1)
    }
}
