package io.github.vooft.kotstruct.descriptor

import io.github.vooft.kotstruct.FieldMapping
import io.github.vooft.kotstruct.KotStructDescribedBy
import io.github.vooft.kotstruct.KotStructDescriptor
import io.github.vooft.kotstruct.KotStructMapper
import io.github.vooft.kotstruct.MappingsDefinitions
import io.github.vooft.kotstruct.descriptor.FieldMappingMapper.SourceDto
import io.github.vooft.kotstruct.descriptor.FieldMappingMapper.TargetDto

@KotStructDescribedBy(FieldMappingMapperDescriptor::class)
interface FieldMappingMapper : KotStructMapper {

    fun map(source: SourceDto): TargetDto

    data class SourceDto(val srcId: String, val nested: Nested) {
        data class Nested(val srcName: String)
    }
    data class TargetDto(val id: String, val nested: Nested) {
        data class Nested(val name: String)
    }
}

object FieldMappingMapperDescriptor : KotStructDescriptor {
    override val mappings = MappingsDefinitions(
        fieldMappings = listOf(
            FieldMapping.create<SourceDto, TargetDto>(listOf(SourceDto::srcId), listOf(TargetDto::id)),
            FieldMapping.create<SourceDto, TargetDto>(
                fromPath = listOf(SourceDto::nested, SourceDto.Nested::srcName),
                toPath = listOf(TargetDto::nested, TargetDto.Nested::name)
            )
        )
    )
}
