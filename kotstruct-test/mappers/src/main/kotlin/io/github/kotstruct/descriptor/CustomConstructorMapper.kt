package io.github.kotstruct.descriptor

import io.github.kotstruct.KotStructDescribedBy
import io.github.kotstruct.KotStructDescriptor
import io.github.kotstruct.KotStructDescriptor.Companion.kotStruct
import io.github.kotstruct.KotStructMapper
import io.github.kotstruct.descriptor.CustomConstructorMapper.TargetDto
import java.util.UUID

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

object CustomConstructorMapperDescriptor : KotStructDescriptor by kotStruct({
    factoryFor { TargetDto::myFactory }
}) {
    val DEFAULT_NAME = this::class.qualifiedName!!
}
