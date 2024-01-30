package io.github.kotstruct.example

import io.github.kotstruct.KotStructMapper
import java.util.UUID
interface ExampleMapper: KotStructMapper {

    fun map(src: FromDto): ToDto

    data class FromDto(val id: UUID, val name: String, val extraProperty: UUID)
    data class ToDto(val name: String, val id: UUID)
}
