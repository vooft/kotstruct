package io.github.vooft.kotstruct.example

import io.github.vooft.kotstruct.KotStructMapper
import java.util.UUID

data class FromDto(val id: UUID, val name: String, val extraProperty: UUID)
data class ToDto(val name: String, val id: UUID)

interface ExampleMapper: KotStructMapper<FromDto, ToDto>
