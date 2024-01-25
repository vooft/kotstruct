package io.github.vooft.kotstruct.descriptor

import io.github.vooft.kotstruct.KotStructGeneratedNestedCustomMapperMapper
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.UUID

class NestedCustomMapperTest {
    @Test
    fun `should use custom mapper`() {
        val mapper = KotStructGeneratedNestedCustomMapperMapper()

        val from = NestedCustomMapperMapper.SourceDto(
            id = UUID.randomUUID(),
            uuidToString = UUID.randomUUID(),
            stringToUUID = UUID.randomUUID().toString()
        )

        val to = mapper.map(from)

        to.id shouldBe from.id
        to.uuidToString shouldBe from.uuidToString.toString()
        to.stringToUUID shouldBe UUID.fromString(from.stringToUUID)
    }
}
