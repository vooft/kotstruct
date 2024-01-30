package io.github.kotstruct.descriptor

import io.github.kotstruct.FieldMapping
import io.github.kotstruct.KotStructDescribedBy
import io.github.kotstruct.KotStructDescriptor
import io.github.kotstruct.KotStructMapper
import io.github.kotstruct.MappingsDefinitions
import io.github.kotstruct.TypeMapping
import io.github.kotstruct.descriptor.FieldMappingMapper.SourceDto
import io.github.kotstruct.descriptor.FieldMappingMapper.TargetDto
import java.util.UUID

@KotStructDescribedBy(FieldMappingMapperDescriptor::class)
interface FieldMappingMapper : KotStructMapper {

    fun map(source: SourceDto): TargetDto

    data class SourceDto(val srcId: String, val nested: Nested) {
        data class Nested(val srcUuid: String)
    }
    data class TargetDto(val id: String, val nested: Nested) {
        data class Nested(val uuid: UUID)
    }
}

object FieldMappingMapperDescriptor : KotStructDescriptor {
    override val mappings = MappingsDefinitions(
        typeMappings = listOf(
            TypeMapping.create<String, UUID> { UUID.fromString(it) }
        ),
        fieldMappings = listOf(
            FieldMapping.create<SourceDto, TargetDto>(listOf(SourceDto::srcId), listOf(TargetDto::id)),
            FieldMapping.create<SourceDto, TargetDto>(
                fromPath = listOf(SourceDto::nested, SourceDto.Nested::srcUuid),
                toPath = listOf(TargetDto::nested, TargetDto.Nested::uuid)
            )
        )
    )
}
