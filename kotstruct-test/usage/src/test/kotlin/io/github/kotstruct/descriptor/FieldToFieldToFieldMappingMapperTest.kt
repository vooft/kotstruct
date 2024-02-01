package io.github.kotstruct.descriptor

import io.github.kotstruct.KotStructGeneratedFieldToFieldMappingMapper
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import org.junit.jupiter.api.Test
import java.util.UUID

class FieldToFieldToFieldMappingMapperTest {
    @Test
    fun `should use field mapping`() {
        val mapper = KotStructGeneratedFieldToFieldMappingMapper()

        val from = FieldToFieldMappingMapper.SourceDto(
            srcId = UUID.randomUUID().toString(),
            nested = FieldToFieldMappingMapper.SourceDto.Nested(
                srcUuid = UUID.randomUUID().toString(),
                toParent = UUID.randomUUID().toString()
            ),
            toChild = UUID.randomUUID().toString()
        )

        val expected = FieldToFieldMappingMapper.TargetDto(
            id = from.srcId,
            nested = FieldToFieldMappingMapper.TargetDto.Nested(
                uuid = UUID.fromString(from.nested.srcUuid),
                fromParent = from.toChild
            ),
            fromChild = from.nested.toParent
        )

        val actual = mapper.map(from)
        actual shouldBeEqualToComparingFields expected
    }
}
