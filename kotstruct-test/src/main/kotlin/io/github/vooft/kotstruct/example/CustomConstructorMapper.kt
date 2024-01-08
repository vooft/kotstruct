package io.github.vooft.kotstruct.example

import io.github.vooft.kotstruct.KotStructCustomConstructor
import io.github.vooft.kotstruct.KotStructMapper
import java.util.UUID

interface CustomConstructorMapper : KotStructMapper<CustomConstructorMapper.FromDto, CustomConstructorMapper.ToDto> {
    @KotStructCustomConstructor
    fun constructor(id: UUID) = ToDto(id)

    data class FromDto(val id: UUID, val extraProperty: UUID)
    data class ToDto(val name: String, val id: UUID) {
        constructor(id: UUID): this(id = id, name = CUSTOM_CONSTRUCTOR_DEFAULT_NAME)
    }
}

const val CUSTOM_CONSTRUCTOR_DEFAULT_NAME = "my_default_name"
