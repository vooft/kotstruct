package io.github.kotstruct.descriptor

import io.github.kotstruct.KotStructDescribedBy
import io.github.kotstruct.KotStructDescriptor
import io.github.kotstruct.KotStructMapper
import io.github.kotstruct.MappingsDefinitions
import io.github.kotstruct.TypeMapping
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
        )
    )
}
