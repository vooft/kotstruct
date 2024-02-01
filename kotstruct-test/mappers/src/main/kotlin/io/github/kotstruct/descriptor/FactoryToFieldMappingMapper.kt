package io.github.kotstruct.descriptor

import io.github.kotstruct.KotStructDescribedBy
import io.github.kotstruct.KotStructDescriptor
import io.github.kotstruct.KotStructDescriptor.Companion.kotStruct
import io.github.kotstruct.KotStructMapper
import io.github.kotstruct.descriptor.FactoryToFieldMappingMapper.SourceDto
import io.github.kotstruct.descriptor.FactoryToFieldMappingMapper.TargetDto
import java.time.Instant
import java.util.UUID

@KotStructDescribedBy(FactoryToFieldMappingMapperDescriptor::class)
interface FactoryToFieldMappingMapper : KotStructMapper {

    fun map(source: SourceDto): TargetDto

    data class SourceDto(val name: String, val nested: Nested) {
        data class Nested(val customDate: Instant)
    }

    data class TargetDto(val id: UUID, val name: String, val nested: Nested) {
        data class Nested(val customDate: Instant, val createdAt: Instant)
    }
}

object FactoryToFieldMappingMapperDescriptor : KotStructDescriptor by kotStruct({
    mappingFor<SourceDto, TargetDto> {
        mapFactory { UUID.randomUUID() } into { TargetDto::id }
        mapFactory { Instant.now() } into { TargetDto::nested / TargetDto.Nested::createdAt }
    }
})
