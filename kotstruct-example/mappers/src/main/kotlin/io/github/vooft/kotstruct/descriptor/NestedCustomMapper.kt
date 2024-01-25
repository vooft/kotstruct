package io.github.vooft.kotstruct.descriptor

import io.github.vooft.kotstruct.KotStructDescribedBy
import io.github.vooft.kotstruct.KotStructDescriptor
import io.github.vooft.kotstruct.KotStructMapper
import io.github.vooft.kotstruct.Mapping
import io.github.vooft.kotstruct.mappingInto
import java.util.UUID
import kotlin.reflect.typeOf

@KotStructDescribedBy(NestedCustomMapperMapperDescriptor::class)
interface NestedCustomMapperMapper : KotStructMapper {
    fun map(src: SourceDto): TargetDto

    data class SourceDto(val id: UUID, val uuidToString: UUID, val stringToUUID: String)
    data class TargetDto(val id: UUID, val uuidToString: String, val stringToUUID: UUID)
}

object NestedCustomMapperMapperDescriptor : KotStructDescriptor {
    override val mappings = mapOf(
        typeOf<String>().mappingInto(typeOf<UUID>()) to
                Mapping.customMapper<String, UUID> { UUID.fromString(it) },
        typeOf<UUID>().mappingInto(typeOf<String>()) to
                Mapping.customMapper<UUID, String> { it.toString() }
    )
}
