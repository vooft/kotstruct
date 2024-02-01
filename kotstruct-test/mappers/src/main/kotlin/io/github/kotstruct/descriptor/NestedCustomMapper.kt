package io.github.kotstruct.descriptor

import io.github.kotstruct.KotStructDescribedBy
import io.github.kotstruct.KotStructDescriptor
import io.github.kotstruct.KotStructDescriptor.Companion.kotStruct
import io.github.kotstruct.KotStructMapper
import java.util.UUID

@KotStructDescribedBy(NestedCustomMapperMapperDescriptor::class)
interface NestedCustomMapperMapper : KotStructMapper {
    fun map(src: SourceDto): TargetDto

    data class SourceDto(val id: UUID, val uuidToString: UUID, val stringToUUID: String)
    data class TargetDto(val id: UUID, val uuidToString: String, val stringToUUID: UUID)
}

object NestedCustomMapperMapperDescriptor : KotStructDescriptor by kotStruct({
    mapperFor<String, UUID> { UUID.fromString(it) }
    mapperFor<UUID, String> { it.toString() }
})
