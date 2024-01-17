package io.github.vooft.kotstruct.descriptor

import io.github.vooft.kotstruct.KotStructDescriptor
import io.github.vooft.kotstruct.KotStructMapper
import io.github.vooft.kotstruct.KotStructMapperDescriptor
import io.github.vooft.kotstruct.descriptor.CustomConstructorMapper.SourceDto
import io.github.vooft.kotstruct.descriptor.CustomConstructorMapper.TargetDto
import java.util.UUID
import kotlin.reflect.KFunction1

@KotStructMapperDescriptor(CustomConstructorMapperDescriptor::class)
interface CustomConstructorMapper : KotStructMapper<SourceDto, TargetDto> {
    data class SourceDto(val id: UUID)
    data class TargetDto(val id: UUID, val name: String) {
        constructor(id: UUID): this(id = id, name = CustomConstructorMapperDescriptor.DEFAULT_NAME)
    }
}

object CustomConstructorMapperDescriptor : KotStructDescriptor<TargetDto> {
    override val constructor: KFunction1<UUID, TargetDto> = ::TargetDto

    const val DEFAULT_NAME = "this is a default name"
}
