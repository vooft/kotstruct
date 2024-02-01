package io.github.kotstruct.descriptor

import io.github.kotstruct.KotStructGeneratedCustomMapperMapper
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.UUID

class CustomMapperMapperTest {
    @Test
    fun `should use custom mapper`() {
        val mapper = KotStructGeneratedCustomMapperMapper()

        val from = CustomMapperMapper.SourceDto(
            id = UUID.randomUUID()
        )

        val to = mapper.map(from)

        to.id shouldBe from.id
        to.name shouldBe CustomMapperMapperDescriptor.DEFAULT_NAME
    }
}
