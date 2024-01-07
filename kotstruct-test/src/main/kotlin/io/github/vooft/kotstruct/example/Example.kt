package io.github.vooft.kotstruct.example

import io.github.vooft.kotstruct.KotStructMapper
import java.util.UUID

data class FromDto(val id: UUID, val name: String)
data class ToDto(val id: UUID, val name: String)

interface ExampleMapper: KotStructMapper<FromDto, ToDto>
