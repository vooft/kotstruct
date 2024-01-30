package io.github.vooft.kotstruct.generator

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import io.github.vooft.kotstruct.IDENTIFIER_COUNTER
import io.github.vooft.kotstruct.KotStructDescriptor
import io.github.vooft.kotstruct.TypeMapperDefinition
import io.github.vooft.kotstruct.TypePair
import io.github.vooft.kotstruct.initializerFrom
import io.github.vooft.kotstruct.primaryConstructor
import io.github.vooft.kotstruct.toMapperTypeName
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure

class KotStructGeneratorSession(
    private val logger: KSPLogger,
    private val sourceClassType: KType,
    private val targetClassType: KType,
    private val descriptor: KotStructDescriptor
) {

    private val descriptorClass = descriptor::class

    // TODO: move to parent object
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
            .addTypeMappers()

            // add invoke operator
            .addInvokeMethod()

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

    private fun TypeSpec.Builder.addTypeMappers(): TypeSpec.Builder {
        for ((typePair, definition) in typeMappers) {
            addProperty(
                PropertySpec.builder(definition.identifier, typePair.toMapperTypeName())
                    .addKdoc("%T -> %T", typePair.from.asTypeName(), typePair.to.asTypeName())
                    .initializer(typePair.initializerFrom(descriptorClass))
                    .build()
            )
        }

        return this
    }

    private fun TypeSpec.Builder.addInvokeMethod(): TypeSpec.Builder {
        val typeMapping = typeMappers[TypePair(sourceClassType, targetClassType)]
        if (typeMapping != null) {
            // when direct type mapping is defined, just use it
            logger.info("Found typeMapping for $sourceClassType and $targetClassType: $typeMapping")

            addFunction(
                FunSpec.builder("invoke")
                    .addModifiers(KModifier.OPERATOR)
                    .addParameter(INPUT_PARAMETER, sourceClassType.asTypeName())
                    .addCode("return %L(input)", typeMapping.identifier)
                    .returns(targetClassType.asTypeName())
                    .build()
            )
        } else {
            // generate custom mapper using constructor

            logger.info("No typeMapping found for $sourceClassType and $targetClassType")

            addFunction(
                FunSpec.builder("invoke")
                    .addModifiers(KModifier.OPERATOR)
                    .addParameter(INPUT_PARAMETER, sourceClassType.asTypeName())
                    .addCode(
                        CodeBlock.builder()
                            .add("return %T(", targetClassType.asTypeName())
                            .apply {
                                val fromProperties = sourceClassType.jvmErasure.memberProperties.associate { it.name to it.returnType }

                                val factory = targetClassType.primaryConstructor
                                for (parameter in factory.parameters) {
                                    val fromType = requireNotNull(fromProperties[parameter.name]) {
                                        "Can't find matching property ${parameter.name} in $sourceClassType"
                                    }

                                    if (fromType == parameter.type) {
                                        add("%L = %L.%L, ", parameter.name, INPUT_PARAMETER, parameter.name)
                                    } else {
                                        val typeMapper = typeMappers[TypePair(fromType, parameter.type)]
                                        if (typeMapper != null) {
                                            add(
                                                "%L = %L(%L.%L), ",
                                                parameter.name,
                                                typeMapper.identifier,
                                                INPUT_PARAMETER,
                                                parameter.name
                                            )
                                        } else {
                                            val subGenerator = KotStructGeneratorSession(
                                                logger = logger,
                                                sourceClassType = fromType,
                                                targetClassType = parameter.type,
                                                descriptor = descriptor
                                            ).generateMapperObject()

                                            addType(subGenerator)
                                            add("%L = %L(%L.%L), ", parameter.name, subGenerator.name, INPUT_PARAMETER, parameter.name)
                                        }
                                    }
                                }

                                add(")")
                            }
                            .build()
                    )
                    .returns(targetClassType.asTypeName())
                    .build()
            )
        }
        return this
    }
}

private const val INPUT_PARAMETER = "input"
