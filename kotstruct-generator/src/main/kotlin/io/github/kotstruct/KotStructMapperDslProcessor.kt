package io.github.kotstruct

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import io.github.kotstruct.generator.KotStructGenerator
import org.reflections.Reflections
import kotlin.reflect.KClass

class KotStructMapperDslProcessor(
    private val prefix: String,
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {
    private var executed = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (executed) {
            return emptyList()
        }

        executed = true

        val reflections = prefix.takeIf { it.isNotBlank() }?.let { Reflections(it) } ?: Reflections()
        val mappers = reflections.getSubTypesOf(KotStructMapper::class.java)
        println("mappers: $mappers")
        val validMappers = buildList {
            for (mapper in mappers) {
                println(mapper)
                if (mapper.isInterface) {
                    add(mapper)
                } else {
                    println("$mapper is not an interface")
                }
            }
        }

        validMappers.forEach {
            val generator = KotStructGenerator(codeGenerator, logger)
            generator.process(it.kotlin)
        }

        println(validMappers.toString())
        return emptyList()
    }
}

class KotStructMapperDslProcessorProvider(private val prefix: String = "") : SymbolProcessorProvider {
    constructor(clazz: KClass<*>) : this(clazz.packageName)

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return KotStructMapperDslProcessor(prefix, environment.codeGenerator, environment.logger)
    }

}
