package io.github.kotstruct.descriptor

import io.github.kotstruct.KotStructDescribedBy
import io.github.kotstruct.KotStructDescriptor
import io.github.kotstruct.KotStructDescriptor.Companion.kotStruct
import io.github.kotstruct.KotStructMapper
import io.github.kotstruct.descriptor.FieldToFieldMappingMapper.SourceDto
import io.github.kotstruct.descriptor.FieldToFieldMappingMapper.TargetDto
import java.util.UUID

@KotStructDescribedBy(FieldToFieldMappingMapperDescriptor::class)
interface FieldToFieldMappingMapper : KotStructMapper {

    fun map(source: SourceDto): TargetDto

    data class SourceDto(val srcId: String, val nested: Nested, val toChild: String) {
        data class Nested(val srcUuid: String, val toParent: String)
    }

    data class TargetDto(val id: String, val nested: Nested, val fromChild: String) {
        data class Nested(val uuid: UUID, val fromParent: String)
    }
}

object FieldToFieldMappingMapperDescriptor : KotStructDescriptor by kotStruct({
    mappingFor<SourceDto, TargetDto> {
        mapField { SourceDto::srcId } into { TargetDto::id }

        mapField { SourceDto::nested / SourceDto.Nested::srcUuid } into { TargetDto::nested / TargetDto.Nested::uuid }

        mapField { SourceDto::toChild } into { TargetDto::nested / TargetDto.Nested::fromParent}

        mapField { SourceDto::nested / SourceDto.Nested::toParent } into { TargetDto::fromChild }
    }

    mapperFor<String, UUID> { UUID.fromString(it) }
})
