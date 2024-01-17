package io.github.vooft.kotstruct.dsl

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import io.github.vooft.kotstruct.KotStructMapper
import io.github.vooft.kotstruct.ksp.GENERATED_PREFIX
import io.github.vooft.kotstruct.ksp.findKotStructMapperSupertype
import io.github.vooft.kotstruct.ksp.packageName
import org.reflections.Reflections
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

class KotStructMapperDslProcessor(private val prefix: String, private val codeGenerator: CodeGenerator) : SymbolProcessor {
    private var executed = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (executed) {
            return emptyList()
        }

        executed = true

        val reflections = prefix.takeIf { it.isNotBlank() }?.let { Reflections(it) } ?: Reflections()
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

        validMappers.forEach { process(it.kotlin) }

        println(validMappers.toString())
        return emptyList()
    }

    private fun process(mapperClass: KClass<out KotStructMapper<*, *>>) {
        val mapperSupertype = requireNotNull(mapperClass.findKotStructMapperSupertype()) {
            "Class ${mapperClass.qualifiedName} must inherit from ${KotStructMapper::class}"
        }

        val sourceClassType = mapperSupertype.arguments.first().type!!
        val targetClassType = mapperSupertype.arguments.last().type!!

        val fromProperties = sourceClassType.jvmErasure.memberProperties.associate { it.name to it.returnType }

        // TODO: handle custom-defined constructor
//        val customConstructor = mapperClass.declarations
//            .filterIsInstance<KSFunctionDeclaration>()
//            .singleOrNull { function ->
//                function.annotations.any { it.annotationType.qualifiedName == KotStructCustomConstructor::class.qualifiedName }
//            }
//
//        if (customConstructor != null) {
//            require(customConstructor.returnType?.resolve()?.declaration == toClass) {
//                "Custom constructor must return $toClass, but returns ${customConstructor.returnType}"
//            }
//        }

//        val toTypeConstructor = customConstructor
//            ?: requireNotNull(toClass.primaryConstructor) { "Primary constructor must be present in $toClass" }

        val toTypeConstructor = requireNotNull(targetClassType.jvmErasure.primaryConstructor) {
            "Primary constructor must be present in $targetClassType"
        }

        val toArguments = toTypeConstructor.parameters.associate { it.name!! to it.type }

        for ((name, toType) in toArguments) {
            val fromType = requireNotNull(fromProperties[name]) { "Can't find matching property $name in $sourceClassType" }
            require(fromType == toType) { "Source property $name type $fromType doesn't match target type $toType" }
        }

        val genertedClassName = "$GENERATED_PREFIX${mapperClass.simpleName}"
        FileSpec.builder(mapperClass.packageName, genertedClassName)
            .addType(
                TypeSpec.classBuilder(genertedClassName)
                    .addFunction(
                        FunSpec.builder("map")
                            .addParameter("from", sourceClassType.asTypeName())
                            .addCode(
                                CodeBlock.builder()
//                            .apply {
//                                when(toTypeConstructor.isConstructor()) {
//                                    true -> add("return %T(", toClass.toClassName())
//                                    false -> add("return %N(", toTypeConstructor.simpleName.asString())
//                                }
//                            }
                                    .add("return %T(", targetClassType.asTypeName())
                                    .apply {
                                        // TODO: improve
                                        for (p in toTypeConstructor.parameters) {
                                            add("from.%N, ", p.name)
                                        }
                                    }
                                    .add(")")
                                    .build())
                            .returns(targetClassType.asTypeName())
                            .build()
                    )
                    .build()
            )
            .build()
            .writeTo(codeGenerator, false)
    }
}

class KotStructMapperDslProcessorProvider(private val prefix: String = "") : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return KotStructMapperDslProcessor(prefix, environment.codeGenerator)
    }

}
