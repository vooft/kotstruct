package io.github.kotstruct

import org.junit.jupiter.api.DynamicTest

fun <T: Any> dynamicTests(vararg pairs: Pair<String, T>, block: (T) -> Unit): List<DynamicTest> = pairs.map { (name, value) ->
    DynamicTest.dynamicTest(name) { block(value) }
}
