package io.github.vooft.kotstruct.example

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.UUID

class ExampleTest {
    @Test
    fun `should map data classes`() {
        val mapper = KotStructGeneratedExampleMapper()

        val from = FromDto(UUID.randomUUID(), UUID.randomUUID().toString(), UUID.randomUUID())

        val to = mapper.map(from)

        to.id shouldBe from.id
        to.name shouldBe from.name
    }
}
