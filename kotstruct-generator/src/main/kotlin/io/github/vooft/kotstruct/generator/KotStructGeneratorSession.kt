package io.github.vooft.kotstruct.generator

import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
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
import io.github.vooft.kotstruct.primaryConstructor
import io.github.vooft.kotstruct.toFunctionTypeName
import io.github.vooft.kotstruct.toTypeName
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
        descriptor.mappings.typeMappings.associate {
            TypePair(it.from, it.to) to TypeMappingDefinition("typeMapper${IDENTIFIER_COUNTER.incrementAndGet()}")
        }
    }

    private val factoryMappings by lazy {
        descriptor.mappings.factoryMappings.associate {
            it.to to FactoryMappingDefinition("factoryMapper${IDENTIFIER_COUNTER.incrementAndGet()}", it.factory)
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

    private fun TypeSpec.Builder.addStaticMappers(): TypeSpec.Builder {
        for ((typePair, definition) in typeMappings) {
            addProperty(
                PropertySpec.builder(definition.identifier, typePair.toFunctionTypeName())
                    .addKdoc("%T -> %T", typePair.from.asTypeName(), typePair.to.asTypeName())
                    .initializer(typePair.typeMappingInitializer(descriptorClass))
                    .build()
            )
        }

        for ((type, definition) in factoryMappings) {
            val factoryTypeName = definition.factory.toTypeName()
            addProperty(
                PropertySpec.builder(definition.identifier, factoryTypeName)
                    .addKdoc("%T Factory", type.asTypeName())
                    .initializer(type.factoryMappingInitializer(descriptorClass, factoryTypeName))
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
            val factory = factoryMapping?.factory ?: targetClassType.primaryConstructor
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

private fun TypePair.typeMappingInitializer(
    descriptorClass: KClass<out KotStructDescriptor>
) = CodeBlock.builder()
    .add("%T.$DESCRIPTOR_MAPPINGS_FIELD.$TYPE_MAPPINGS_FIELD.single·{ ", descriptorClass)
    .add("it.from·==·typeOf<%T>()·&&·it.to·==·typeOf<%T>()", from.asTypeName(), to.asTypeName())
    .add("}.$TYPE_MAPPING_MAPPER_FIELD as %T", toFunctionTypeName())
    .build()

private fun KType.factoryMappingInitializer(
    descriptorClass: KClass<out KotStructDescriptor>,
    factoryTypeName: TypeName
) = CodeBlock.builder()
    .add("%T.$DESCRIPTOR_MAPPINGS_FIELD.$FACTORY_MAPPINGS_FIELD.single·{ ", descriptorClass)
    .add("it.to·==·typeOf<%T>()", asTypeName())
    .add("}.$FACTORY_MAPPING_FACTORY_FIELD as %T", factoryTypeName)
    .build()

private val DESCRIPTOR_MAPPINGS_FIELD = KotStructDescriptor::mappings.name
private val TYPE_MAPPINGS_FIELD = MappingsDefinitions::typeMappings.name
private val FACTORY_MAPPINGS_FIELD = MappingsDefinitions::factoryMappings.name
private val TYPE_MAPPING_MAPPER_FIELD = TypeMapping<*, *>::mapper.name
private val FACTORY_MAPPING_FACTORY_FIELD = FactoryMapping<*>::factory.name
