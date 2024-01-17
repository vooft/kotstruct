package io.github.vooft.kotstruct

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.junit.jupiter.api.DynamicTest

fun createCompilation(src: SourceFile, vararg miscFiles: SourceFile): KotlinCompilation {
    return KotlinCompilation().apply {
        sources = listOf(src) + miscFiles.toList()
        symbolProcessorProviders = listOf(KotStructMapperDslProcessorProvider())
        inheritClassPath = true
        messageOutputStream = System.out // see diagnostics in real time
    }
}

fun compile(src: SourceFile, vararg miscFiles: SourceFile): KotlinCompilation.Result {
    return createCompilation(src, *miscFiles).compile()
}

fun Map<String, SourceFile>.dynamicTests(body: (SourceFile) -> Unit) = map { (name, fromDtoCode) ->
    DynamicTest.dynamicTest(name) { body(fromDtoCode) }
}

fun <T: Any> dynamicTests(vararg pairs: Pair<String, T>, block: (T) -> Unit): List<DynamicTest> = pairs.map { (name, value) ->
    DynamicTest.dynamicTest(name) { block(value) }
}
