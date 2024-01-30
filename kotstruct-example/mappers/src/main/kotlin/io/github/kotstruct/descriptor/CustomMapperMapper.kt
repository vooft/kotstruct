package io.github.kotstruct.descriptor

import io.github.kotstruct.KotStructDescribedBy
import io.github.kotstruct.KotStructDescriptor
import io.github.kotstruct.KotStructMapper
import io.github.kotstruct.MappingsDefinitions
import io.github.kotstruct.TypeMapping
import io.github.kotstruct.descriptor.CustomMapperMapper.SourceDto
import io.github.kotstruct.descriptor.CustomMapperMapper.TargetDto
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
        )
    )
    val DEFAULT_NAME = this::class.qualifiedName!!
}

