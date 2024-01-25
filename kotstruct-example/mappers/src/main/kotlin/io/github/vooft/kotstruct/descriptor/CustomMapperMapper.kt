package io.github.vooft.kotstruct.descriptor

import io.github.vooft.kotstruct.KotStructDescribedBy
import io.github.vooft.kotstruct.KotStructDescriptor
import io.github.vooft.kotstruct.KotStructMapper
import io.github.vooft.kotstruct.Mapping
import io.github.vooft.kotstruct.descriptor.CustomMapperMapper.SourceDto
import io.github.vooft.kotstruct.descriptor.CustomMapperMapper.TargetDto
import io.github.vooft.kotstruct.mappingInto
import java.util.UUID
import kotlin.reflect.typeOf

@KotStructDescribedBy(CustomMapperMapperDescriptor::class)
interface CustomMapperMapper : KotStructMapper {
    fun map(src: SourceDto): TargetDto

    data class SourceDto(val id: UUID)
    data class TargetDto(val id: UUID, val name: String)
}

object CustomMapperMapperDescriptor : KotStructDescriptor {
    override val mappings = mapOf(
        typeOf<SourceDto>().mappingInto(typeOf<TargetDto>()) to
                Mapping.customMapper<SourceDto, TargetDto> { TargetDto(it.id, DEFAULT_NAME) })
    val DEFAULT_NAME = this::class.qualifiedName!!
}

