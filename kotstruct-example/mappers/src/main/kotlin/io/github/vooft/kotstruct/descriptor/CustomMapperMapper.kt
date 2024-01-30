package io.github.vooft.kotstruct.descriptor

import io.github.vooft.kotstruct.KotStructDescribedBy
import io.github.vooft.kotstruct.KotStructDescriptor
import io.github.vooft.kotstruct.KotStructMapper
import io.github.vooft.kotstruct.MappingsDefinitions
import io.github.vooft.kotstruct.TypeMapping
import io.github.vooft.kotstruct.descriptor.CustomMapperMapper.SourceDto
import io.github.vooft.kotstruct.descriptor.CustomMapperMapper.TargetDto
import java.util.UUID

@KotStructDescribedBy(CustomMapperMapperDescriptor::class)
interface CustomMapperMapper : KotStructMapper {
    fun map(src: SourceDto): TargetDto

    data class SourceDto(val id: UUID)
    data class TargetDto(val id: UUID, val name: String)
}

object CustomMapperMapperDescriptor : KotStructDescriptor {
    override val mappings = MappingsDefinitions(
        typeMappings = listOf(
            TypeMapping.create<SourceDto, TargetDto> { TargetDto(it.id, DEFAULT_NAME) }
        ),
        factoryMappings = emptyList(),
        fieldMappings = emptyList()
    )
    val DEFAULT_NAME = this::class.qualifiedName!!
}

