package io.github.vooft.kotstruct.generator

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import io.github.vooft.kotstruct.KotStructDescriptor
import io.github.vooft.kotstruct.primaryConstructor
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure

private val IDENTIFIER_COUNTER = AtomicInteger()

class KotStructGeneratorSession(
    private val logger: KSPLogger,
    private val sourceClassType: KType,
    private val targetClassType: KType,
    private val descriptor: KotStructDescriptor
) {

    private val descriptorClass = descriptor::class

    // make lazy to make sure that the counter is only incremented if needed
    private val typeMappers by lazy {
        descriptor.mappings.typeMappings.associate {
            TypePair(it.from, it.to) to TypeMapperDefinition("mapper${IDENTIFIER_COUNTER.incrementAndGet()}")
        }
    }

    fun generateMapperObject(): TypeSpec {
        return TypeSpec.objectBuilder("GeneratedMapper${IDENTIFIER_COUNTER.incrementAndGet()}")
            .addModifiers(KModifier.PRIVATE)

            // add properties for type mappers
            .apply {
                for ((typePair, definition) in typeMappers) {
                    addProperty(
                        PropertySpec.builder(definition.identifier, typePair.toMapperTypeName())
                            .addKdoc("%T -> %T", typePair.from.asTypeName(), typePair.to.asTypeName())
                            .initializer(typePair.initializerFrom(descriptorClass))
                            .build()
                    )
                }
            }

            // add invoke operator
            .apply {
                val typeMapping = typeMappers[TypePair(sourceClassType, targetClassType)]
                if (typeMapping != null) {
                    logger.info("Found typeMapping for $sourceClassType and $targetClassType: $typeMapping")
                    addFunction(FunSpec.builder("invoke")
                        .addModifiers(KModifier.OPERATOR)
                        .addParameter(INPUT_PARAMETER, sourceClassType.asTypeName())
                        .addCode("return %L(input)", typeMapping.identifier)
                        .returns(targetClassType.asTypeName())
                        .build()
                    )
                } else {
                    logger.info("No typeMapping found for $sourceClassType and $targetClassType")

                    addFunction(FunSpec.builder("invoke")
                        .addModifiers(KModifier.OPERATOR)
                        .addParameter(INPUT_PARAMETER, sourceClassType.asTypeName())
                        .addCode(CodeBlock.builder()
                            .add("return %T(", targetClassType.asTypeName())
                            .apply {
                                val fromProperties = sourceClassType.jvmErasure.memberProperties.associate { it.name to it.returnType }

                                val factory = targetClassType.primaryConstructor
                                for (parameter in factory.parameters) {
                                    val fromProperty = requireNotNull(fromProperties[parameter.name]) {
                                        "Can't find matching property $${parameter.name} in $sourceClassType"
                                    }

                                    require(fromProperty == parameter.type) {
                                        "Type mismatch for parameter ${parameter.name}: $fromProperty!= ${parameter.type}"
                                    }

                                    add("%L = %L.%L, ", parameter.name, INPUT_PARAMETER, parameter.name)
                                }

                                add(")")
                            }
                            .build()
                        )
                        .returns(targetClassType.asTypeName())
                        .build()
                    )
                }
            }

            .build()

//        val factoryMapping = aggregatedMappings.findFactoryMapping(targetClassType)
//        if (factoryMapping != null) {
//            logger.info("Found factoryMapping for $targetClassType: $factoryMapping")
//             TODO: do something
//        }

//        val factory = factoryMapping?.factory ?: targetClassType.primaryConstructor
//        logger.info("Found factory for $targetClassType: $factory")
//
//        TypeSpec.objectBuilder("Generated${OBJECT_COUNTER.incrementAndGet()}")
//            .
    }
}

data class TypePair(val from: KType, val to: KType)

fun TypePair.toMapperTypeName() = Function1::class.asClassName()
    .parameterizedBy(from.asTypeName(), to.asTypeName())

fun TypePair.initializerFrom(descriptorClass: KClass<out KotStructDescriptor>) = CodeBlock.builder()
    .add("%T.mappings.typeMappings.single { ", descriptorClass)
    .add("it.from == typeOf<%T>() && it.to == typeOf<%T>()", from.asTypeName(), to.asTypeName())
    .add("}.mapper as %T", toMapperTypeName())
    .build()
data class TypeMapperDefinition(val identifier: String)

private const val INPUT_PARAMETER = "input"
