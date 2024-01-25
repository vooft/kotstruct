package io.github.vooft.kotstruct.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import io.github.vooft.kotstruct.KotStructDescribedBy
import io.github.vooft.kotstruct.KotStructDescriptor
import io.github.vooft.kotstruct.Mapping
import io.github.vooft.kotstruct.MappingImplementation
import io.github.vooft.kotstruct.mappingInto
import io.github.vooft.kotstruct.primaryConstructor
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure

class KotStructGeneratorSession(
    private val method: KFunction<*>,
    private val descriptorClass: KClass<out KotStructDescriptor>
) {
    private val sourceParameterName = requireNotNull(method.parameters.last().name) { "Parameter name can not be null" }

    private val mapMethodSourceType = run {
        // first argument is the receiver, second is actual argument
        require(method.parameters.size == 2) { "Mapping method $method must have exactly 1 argument" }
        method.parameters.last().type
    }

    private val descriptor = requireNotNull(descriptorClass.objectInstance) {
        "@${KotStructDescribedBy::class.simpleName} must reference an object, but $descriptorClass is not"
    }

    private val mapMethodTargetType = method.returnType

    private val rootLevelMappers = descriptor.mappings
        .asSequence()
        .filter { (_, mapper) -> mapper is MappingImplementation.CustomMapper1MappingImpl<*, *> }
        .mapNotNull { (mapperId, mapper) ->
            when (mapper) {
                is MappingImplementation.CustomMapper1MappingImpl<*, *> ->
                    Pair(mapper.sourceType, mapper.targetType) to Pair(mapper, mapperId)
                else -> null
            }
        }
        .toMap()

    fun TypeSpec.Builder.generateMethod() {
        val mappingId = mapMethodSourceType.mappingInto(mapMethodTargetType)
        val customMapping = descriptor.mappings[mappingId]

        addFunction(
            FunSpec.builder("map")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter(sourceParameterName, mapMethodSourceType.asTypeName())
                .addCode(
                    CodeBlock.builder()
                        .apply {
                            when (customMapping) {
                                null -> mapMethodTargetType.addPrimaryConstructor()
                                is Mapping.CustomFactoryMapping<*> -> customMapping.customFactory(mappingId)
                                is Mapping.CustomMapperMapping<*> -> customMapping.customMapper(mappingId)
                            }
                        }
                        .build())
                .returns(mapMethodTargetType.asTypeName())
                .build()
        )
    }

    context(CodeBlock.Builder)
    private fun Mapping.CustomMapperMapping<*>.customMapper(mappingId: String) {
        add("return")
        addGetMappingForCustomMapper(mappingId)
        add(".$MAPPER_PROPERTY($sourceParameterName)")
    }

    context(CodeBlock.Builder)
    private fun Mapping.CustomFactoryMapping<*>.customFactory(mappingId: String) {
        val nestedMappings = factory.findNestedMappings()

        val kFunction = "KFunction" + factory.parameters.size
        val kfunctionClassName = ClassName("kotlin.reflect", kFunction)
            .parameterizedBy(
                // last kfunction parameter is the return type
                factory.parameters.map { it.type.asTypeName() } + factory.returnType.asTypeName()
            )
        add("return (")
        addGetMappingForMappingId(mappingId)
        add(".$FACTORY_PROPERTY as %T)(", kfunctionClassName)
        factory.addArguments(nestedMappings)
        add(")")
    }

    context(CodeBlock.Builder)
    private fun KType.addPrimaryConstructor() {
        val nestedMappings = primaryConstructor.findNestedMappings()

        add("return %T(", asTypeName())
        primaryConstructor.addArguments(nestedMappings)
        add(")")
    }

    context(CodeBlock.Builder)
    private fun Mapping<*>.addGetMappingForMappingId(mappingId: String) {
        when (this) {
            is MappingImplementation.CustomFactoryMappingImpl -> add(
                "(%T.mappings.getValue(\"$mappingId\") as %T)",
                descriptorClass,
                this::class
            )

            is Mapping.CustomMapperMapping -> addGetMappingForCustomMapper(mappingId)
        }
    }

    context(CodeBlock.Builder)
    private fun Mapping.CustomMapperMapping<*>.addGetMappingForCustomMapper(mappingId: String) {
        add("(%T.mappings.getValue(\"$mappingId\")", descriptorClass)

        when (this) {
            is MappingImplementation.CustomMapper1MappingImpl<*, *> -> add(
                "as %T)", this::class.asClassName().parameterizedBy(
                    sourceType.asTypeName(), targetType.asTypeName()
                )
            )
        }
    }

    private fun KFunction<Any>.findNestedMappings(): Map<String, TypePair> = buildMap {
        val fromProperties = mapMethodSourceType.jvmErasure.memberProperties.associate { it.name to it.returnType }

        val toArguments = parameters.associate { it.name!! to it.type }

        for ((name, toType) in toArguments) {
            val fromType = requireNotNull(fromProperties[name]) { "Can't find matching property $name in $mapMethodSourceType" }

            if (fromType != toType) {
                val typePair = TypePair(fromType, toType)
                require(rootLevelMappers.containsKey(typePair)) {
                    "Source property $name type $fromType doesn't match the target type $toType, also no custom mapper found"
                }
                put(name, typePair)
            }
        }
    }

    context(CodeBlock.Builder)
    private fun KFunction<*>.addArguments(nestedMappings: Map<String, TypePair>) {
        for (parameter in parameters) {
            val pair = nestedMappings[parameter.name]?.let { rootLevelMappers.getValue(it) }
            if (pair == null) {
                add("$sourceParameterName.%N, ", parameter.name)
            } else {
                val (mapper, mappingId) = pair
                mapper.addGetMappingForCustomMapper(mappingId)
                add(".$MAPPER_PROPERTY($sourceParameterName.%N), ", parameter.name)
            }
        }
    }
}

typealias TypePair = Pair<KType, KType>

private val FACTORY_PROPERTY = Mapping.CustomFactoryMapping<*>::factory.name
private val MAPPER_PROPERTY = Mapping.CustomMapperMapping<*>::mapper.name
