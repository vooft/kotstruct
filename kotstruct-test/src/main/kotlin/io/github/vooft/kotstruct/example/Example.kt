package io.github.vooft.kotstruct.example

import io.github.vooft.kotstruct.KotStructMapper

data class FromDto(val id: String, val name: String)
data class ToDto(val id: String, val name: String)

interface ExampleMapper: KotStructMapper<FromDto, ToDto>
