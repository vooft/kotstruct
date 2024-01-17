package io.github.vooft.kotstruct.descriptor

import io.github.vooft.kotstruct.KotStructGeneratedCustomConstructorMapper
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.UUID

class CustomConstructorMapperTest {
    @Test
    fun `should use custom constructor`() {
        val mapper = KotStructGeneratedCustomConstructorMapper()

        val from = CustomConstructorMapper.SourceDto(
            id = UUID.randomUUID()
        )

        val to = mapper.map(from)

        to.id shouldBe from.id
        to.name shouldBe CustomConstructorMapperDescriptor.DEFAULT_NAME
    }
}
