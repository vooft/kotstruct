package io.github.vooft.kotstruct.descriptor

import io.github.vooft.kotstruct.KotStructDescribedBy
import io.github.vooft.kotstruct.KotStructDescriptor
import io.github.vooft.kotstruct.KotStructMapper
import io.github.vooft.kotstruct.Mapping
import io.github.vooft.kotstruct.descriptor.CustomConstructorMapper.SourceDto
import io.github.vooft.kotstruct.descriptor.CustomConstructorMapper.TargetDto
import io.github.vooft.kotstruct.mappingInto
import java.util.UUID
import kotlin.reflect.typeOf

@KotStructDescribedBy(CustomConstructorMapperDescriptor::class)
interface CustomConstructorMapper : KotStructMapper {
    fun map(src: SourceDto): TargetDto

    data class SourceDto(val id: UUID)
    data class TargetDto(val id: UUID, val name: String) {
        companion object {
            fun myFactory(id: UUID) = TargetDto(id, CustomConstructorMapperDescriptor.DEFAULT_NAME)
        }
    }
}

object CustomConstructorMapperDescriptor : KotStructDescriptor {
    override val mappings = mapOf(
        typeOf<SourceDto>().mappingInto(typeOf<TargetDto>()) to
                Mapping.customFactory(TargetDto::myFactory))
    val DEFAULT_NAME = this::class.qualifiedName!!
}
