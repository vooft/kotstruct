package io.github.kotstruct.descriptor

import io.github.kotstruct.KotStructGeneratedFieldMappingMapper
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import org.junit.jupiter.api.Test
import java.util.UUID

class FieldMappingMapperTest {
    @Test
    fun `should use field mapping`() {
        val mapper = KotStructGeneratedFieldMappingMapper()

        val from = FieldMappingMapper.SourceDto(
            srcId = UUID.randomUUID().toString(),
            nested = FieldMappingMapper.SourceDto.Nested(
                srcUuid = UUID.randomUUID().toString(),
                toParent = UUID.randomUUID().toString()
            ),
            toChild = UUID.randomUUID().toString()
        )

        val expected = FieldMappingMapper.TargetDto(
            id = from.srcId,
            nested = FieldMappingMapper.TargetDto.Nested(
                uuid = UUID.fromString(from.nested.srcUuid),
                fromParent = from.toChild
            ),
            fromChild = from.nested.toParent
        )

        val actual = mapper.map(from)
        actual shouldBeEqualToComparingFields expected
    }
}
