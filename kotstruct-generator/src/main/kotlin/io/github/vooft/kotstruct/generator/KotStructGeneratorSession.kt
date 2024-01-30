package io.github.vooft.kotstruct.generator

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import io.github.vooft.kotstruct.FactoryMapping
import io.github.vooft.kotstruct.FactoryMappingDefinition
import io.github.vooft.kotstruct.IDENTIFIER_COUNTER
import io.github.vooft.kotstruct.KotStructDescriptor
import io.github.vooft.kotstruct.MappingsDefinitions
import io.github.vooft.kotstruct.TypeMapping
import io.github.vooft.kotstruct.TypeMappingDefinition
import io.github.vooft.kotstruct.TypePair
import io.github.vooft.kotstruct.associateIndexed
import io.github.vooft.kotstruct.primaryConstructor
import io.github.vooft.kotstruct.toFunctionTypeName
import io.github.vooft.kotstruct.toParametrizedTypeName
import kotlin.reflect.KClass
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
    private val typeMappings by lazy {
        descriptor.mappings.typeMappings.associateIndexed { index, mapping ->
            TypePair(from = mapping.from, to = mapping.to) to TypeMappingDefinition(
                index = index,
                identifier = "typeMapper${IDENTIFIER_COUNTER.incrementAndGet()}",
                mapping = mapping
            )
        }
    }

    private val factoryMappings by lazy {
        descriptor.mappings.factoryMappings.associateIndexed { index, mapping ->
            mapping.to to FactoryMappingDefinition(
                index = index,
                identifier = "factoryMapper${IDENTIFIER_COUNTER.incrementAndGet()}",
                mapping = mapping
            )
        }
    }

    fun generateMapperObject(): TypeSpec {
        return TypeSpec.objectBuilder("GeneratedMapper${IDENTIFIER_COUNTER.incrementAndGet()}")
            .addModifiers(KModifier.PRIVATE)

            // add properties for type mappers
            .addStaticMappers()

            // add invoke operator
            .addInvokeMethod()

            .build()
    }

    private fun TypeSpec.Builder.addStaticMappers(): TypeSpec.Builder {
        // type mappings
        for ((typePair, definition) in typeMappings) {
            addProperty(
                PropertySpec.builder(definition.identifier, definition.toFunctionTypeName())
                    .addKdoc("%T -> %T", typePair.from.asTypeName(), typePair.to.asTypeName())
                    .initializer(definition.typeMappingInitializer(descriptorClass))
                    .build()
            )
        }

        // factory mappings
        for ((type, definition) in factoryMappings) {
            addProperty(
                PropertySpec.builder(definition.identifier, definition.mapping.factory.toParametrizedTypeName())
                    .addKdoc("%T Factory", type.asTypeName())
                    .initializer(definition.factoryMappingInitializer(descriptorClass))
                    .build()
            )
        }

        return this
    }

    private fun TypeSpec.Builder.addInvokeMethod(): TypeSpec.Builder {
        val typeMapping = typeMappings[TypePair(sourceClassType, targetClassType)]
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

            val factoryMapping = factoryMappings[targetClassType]
            val factory = factoryMapping?.mapping?.factory ?: targetClassType.primaryConstructor
            addFunction(
                FunSpec.builder("invoke")
                    .addModifiers(KModifier.OPERATOR)
                    .addParameter(INPUT_PARAMETER, sourceClassType.asTypeName())
                    .addCode(
                        CodeBlock.builder()
                            .apply {
                                if (factoryMapping != null) {
                                    add("return %L(", factoryMapping.identifier)
                                } else {
                                    add("return %T(", targetClassType.asTypeName())
                                }
                            }
                            .apply {
                                val fromProperties = sourceClassType.jvmErasure.memberProperties.associate { it.name to it.returnType }
                                for (parameter in factory.parameters) {
                                    val fromType = requireNotNull(fromProperties[parameter.name]) {
                                        "Can't find matching property ${parameter.name} in $sourceClassType"
                                    }

                                    if (fromType == parameter.type) {
                                        add("%L.%L, ", INPUT_PARAMETER, parameter.name)
                                    } else {
                                        val typeMapper = typeMappings[TypePair(fromType, parameter.type)]
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

private fun TypeMappingDefinition.typeMappingInitializer(
    descriptorClass: KClass<out KotStructDescriptor>
) = CodeBlock.builder()
    .add("%T.$DESCRIPTOR_MAPPINGS_FIELD.$TYPE_MAPPINGS_FIELD[%L]", descriptorClass, index)
    .add(".$TYPE_MAPPING_MAPPER_FIELD as %T", toFunctionTypeName())
    .build()

private fun FactoryMappingDefinition.factoryMappingInitializer(
    descriptorClass: KClass<out KotStructDescriptor>,
) = CodeBlock.builder()
    .add("%T.$DESCRIPTOR_MAPPINGS_FIELD.$FACTORY_MAPPINGS_FIELD[%L]", descriptorClass, index)
    .add(".$FACTORY_MAPPING_FACTORY_FIELD as %T", mapping.factory.toParametrizedTypeName())
    .build()

private val DESCRIPTOR_MAPPINGS_FIELD = KotStructDescriptor::mappings.name
private val TYPE_MAPPINGS_FIELD = MappingsDefinitions::typeMappings.name
private val FACTORY_MAPPINGS_FIELD = MappingsDefinitions::factoryMappings.name
private val TYPE_MAPPING_MAPPER_FIELD = TypeMapping<*, *>::mapper.name
private val FACTORY_MAPPING_FACTORY_FIELD = FactoryMapping<*>::factory.name
