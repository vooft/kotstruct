package io.github.vooft.kotstruct.descriptor

import io.github.vooft.kotstruct.KotStructDescribedBy
import io.github.vooft.kotstruct.KotStructDescriptor
import io.github.vooft.kotstruct.KotStructMapper
import io.github.vooft.kotstruct.MappingsDefinitions
import io.github.vooft.kotstruct.TypeMapping
import java.util.UUID

@KotStructDescribedBy(NestedCustomMapperMapperDescriptor::class)
interface NestedCustomMapperMapper : KotStructMapper {
    fun map(src: SourceDto): TargetDto

    data class SourceDto(val id: UUID, val uuidToString: UUID, val stringToUUID: String)
    data class TargetDto(val id: UUID, val uuidToString: String, val stringToUUID: UUID)
}

object NestedCustomMapperMapperDescriptor : KotStructDescriptor {
    override val mappings = MappingsDefinitions(
        typeMappings = listOf(
            TypeMapping.create<String, UUID> { UUID.fromString(it) },
            TypeMapping.create<UUID, String> { it.toString() }
        ),
        factoryMappings = emptyList(),
        fieldMappings = emptyList()
    )
}
