package io.github.vooft.kotstruct.descriptor

import io.github.vooft.kotstruct.KotStructGeneratedFieldMappingMapper
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.UUID

class FieldMappingMapperTest {
    @Test
    fun `should use field mapping`() {
        val mapper = KotStructGeneratedFieldMappingMapper()

        val uuid = UUID.randomUUID()
        val from = FieldMappingMapper.SourceDto(
            srcId = UUID.randomUUID().toString(),
            nested = FieldMappingMapper.SourceDto.Nested(srcUuid = uuid.toString())
        )

        val actual = mapper.map(from)

        actual.id shouldBe from.srcId
        actual.nested.uuid shouldBe uuid
    }
}
