package io.github.vooft.kotstruct.dsl

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import io.github.vooft.kotstruct.KotStructMapper
import org.reflections.Reflections

class KotStructMapperDslProcessor(private val prefix: String) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val reflections = Reflections()
        val mappers = reflections.getSubTypesOf(KotStructMapper::class.java)
        val validMappers = buildList {
            for (mapper in mappers) {
                if (mapper.kotlin.objectInstance != null) {
                    add(mapper)
                } else {
                    println("$mapper is not an object")
                }
            }
        }


        println(mappers.toString())
        return emptyList()
    }
}

class KotStructMapperDslProcessorProvider(private val prefix: String = "") : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return KotStructMapperDslProcessor(prefix)
    }

}
