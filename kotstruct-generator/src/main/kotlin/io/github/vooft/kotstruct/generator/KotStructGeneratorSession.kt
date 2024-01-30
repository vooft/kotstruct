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
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure

class KotStructGeneratorSession(
    private val logger: KSPLogger,
    private val sourceClassType: KType,
    private val targetClassType: KType,
    private val descriptor: KotStructDescriptor,
    private val parentParameters: List<FunctionParameter>,
    private val inputParameter: FunctionParameter = FunctionParameter(INPUT_PREFIX + parentParameters.size, sourceClassType),
    private val toParentPath: List<KProperty<*>>
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

    private val fieldMappings = descriptor.mappings.fieldMappings.associateBy { it.toPath }

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
                    // add all parameters to simplify generation
                    .apply {
                        for (param in parentParameters) {
                            addParameter(param)
                        }
                    }
                    .addParameter(inputParameter)
                    .addCode("return %L(%L)", typeMapping.identifier, inputParameter.name)
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
                    // add all parameters to simplify generation
                    .apply {
                        for ((name, paramType) in parentParameters) {
                            addParameter(name, paramType.asTypeName())
                        }
                    }
                    .addParameter(inputParameter)
                    .addCode(
                        CodeBlock.builder()
                            .apply {
                                if (factoryMapping != null) {
                                    add("return %L(", factoryMapping.identifier)
                                } else {
                                    add("return %T(", targetClassType.asTypeName())
                                }
                            }
                            .generateFactoryArguments(factory)
                            .build()
                    )
                    .returns(targetClassType.asTypeName())
                    .build()
            )
        }
        return this
    }

    context(TypeSpec.Builder)
    private fun CodeBlock.Builder.generateFactoryArguments(factory: KFunction<Any>): CodeBlock.Builder {
        val fromProperties = sourceClassType.jvmErasure.memberProperties.associate { it.name to it.returnType }
        for (factoryParameter in factory.parameters) {
            add("/* %L = */ ", factoryParameter.name)
            // try to find a property matching by name and type
            // should easily work for primary constructor
            // TODO: need custom factory?
            val definedProperty = targetClassType.jvmErasure.declaredMemberProperties.find {
                it.name == factoryParameter.name && it.returnType == it.returnType
            }

            val nestedPath = (toParentPath + definedProperty).filterNotNull()

            // generate field mapping
            // TODO: improve to allow not only full path
            if (fieldMappings.containsKey(nestedPath)) {
                val fieldMapping = fieldMappings.getValue(nestedPath)
                buildString {
                    val typeMapper = typeMappings[TypePair(fieldMapping.fromPath.last().returnType, factoryParameter.type)]
                    if (typeMapper != null) {
                        add("%L(", typeMapper.identifier)
                    }

                    add("%L.", parentParameters.firstOrNull()?.name ?: inputParameter.name)

                    for ((index, property) in fieldMapping.fromPath.withIndex()) {
                        add(property.name)
                        if (index < fieldMapping.fromPath.size - 1) {
                            if (property.returnType.isMarkedNullable) {
                                add("?")
                            }

                            add(".")
                        }
                    }

                    if (typeMapper != null) {
                        add(")")
                    }
                }
            } else {
                val fromType = requireNotNull(fromProperties[factoryParameter.name]) {
                    "Can't find matching property ${factoryParameter.name} in $sourceClassType"
                }

                if (fromType == factoryParameter.type) {
                    // can be mapped directly
                    add("%L.%L", inputParameter.name, factoryParameter.name)
                } else {
                    // there is a type mapping defined
                    val typeMapper = typeMappings[TypePair(fromType, factoryParameter.type)]
                    if (typeMapper != null) {
                        add(
                            "%L(%L.%L)",
                            typeMapper.identifier,
                            inputParameter.name,
                            factoryParameter.name
                        )
                    } else {
                        val nestedInputParameter = FunctionParameter(
                            name = INPUT_PREFIX + (parentParameters.size + 1),
                            type = fromType
                        )
                        // need to generate a custom mapper
                        val subGenerator = KotStructGeneratorSession(
                            logger = logger,
                            sourceClassType = fromType,
                            targetClassType = factoryParameter.type,
                            descriptor = descriptor,
                            parentParameters = parentParameters + inputParameter,
                            inputParameter = nestedInputParameter,
                            toParentPath = nestedPath
                        ).generateMapperObject()

                        // TODO: split code block and type spec
                        addType(subGenerator)

                        // invoke generated object
                        add("%L(", subGenerator.name)

                        // pass all the existing parameters
                        for (currentParamPathItem in parentParameters) {
                            add(
                                "/* path parameter */ %L = %L, ",
                                currentParamPathItem.name,
                                currentParamPathItem.name
                            )
                        }

                        // pass current parameter
                        add("%L = %L, ", inputParameter.name, inputParameter.name)

                        // pass nested parameter
                        add(
                            "%L = %L.%L)",
                            nestedInputParameter.name,
                            inputParameter.name,
                            factoryParameter.name
                        )
                    }
                }
            }

            add(", ")
        }

        add(")")

        return this
    }
}

private const val INPUT_PREFIX = "input"

data class FunctionParameter(val name: String, val type: KType)

private fun FunSpec.Builder.addParameter(parameter: FunctionParameter) = addParameter(parameter.name, parameter.type.asTypeName())

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
