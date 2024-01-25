package io.github.vooft.kotstruct.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
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
    private val sourceClassType = run {
        // first argument is the receiver, second is actual argument
        require(method.parameters.size == 2) { "Mapping method $method must have exactly 1 argument" }
        method.parameters.last().type
    }

    private val targetClassType = method.returnType

    fun generateMethod(): FunSpec {
        val mappingId = sourceClassType.mappingInto(targetClassType)
        val customMapping = run {
            val descriptor = requireNotNull(descriptorClass.objectInstance) {
                "@${KotStructDescribedBy::class.simpleName} must reference an object, but $descriptorClass is not"
            }

            descriptor.mappings[mappingId]
        }

        return FunSpec.builder("map")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("src", sourceClassType.asTypeName())
            .addCode(
                CodeBlock.builder()
                    .apply {
                        when (customMapping) {
                            null -> targetClassType.addPrimaryConstructor()
                            is Mapping.CustomFactoryMapping<*> -> customMapping.customFactory(mappingId)
                            is Mapping.CustomMapperMapping<*> -> customMapping.customMapper(mappingId)
                        }
                    }
                    .build())
            .returns(targetClassType.asTypeName())
            .build()
    }

    context(CodeBlock.Builder)
    private fun Mapping.CustomMapperMapping<*>.customMapper(mappingId: String) {
        add("return (")
        add("%T.mappings.getValue(\"$mappingId\")", descriptorClass)

        when (this) {
            is MappingImplementation.CustomMapper1MappingImpl<*, *> -> add("as %T).mapper(src)", this::class.asClassName()
                .parameterizedBy(sourceClassType.asTypeName(), targetClassType.asTypeName()))
        }
    }

    context(CodeBlock.Builder)
    private fun Mapping.CustomFactoryMapping<*>.customFactory(mappingId: String) {
        factory.validateCouldBePopulatedFrom(sourceClassType)

        val kFunction = "KFunction" + factory.parameters.size
        val kfunctionClassName = ClassName("kotlin.reflect", kFunction)
            .parameterizedBy(
                // last kfunction parameter is the return type
                factory.parameters.map { it.type.asTypeName() } + factory.returnType.asTypeName()
            )
        add("return ((%T.mappings.getValue(\"$mappingId\") as %T).factory as %T)(", descriptorClass, this::class, kfunctionClassName)
        factory.addArguments("src")
        add(")")
    }

    context(CodeBlock.Builder)
    private fun KType.addPrimaryConstructor() {
        primaryConstructor.validateCouldBePopulatedFrom(sourceClassType)

        add("return %T(", asTypeName())
        primaryConstructor.addArguments("src")
        add(")")
    }
}

private fun KFunction<Any>.validateCouldBePopulatedFrom(sourceClassType: KType) {
    val fromProperties = sourceClassType.jvmErasure.memberProperties.associate { it.name to it.returnType }

    val toArguments = parameters.associate { it.name!! to it.type }

    for ((name, toType) in toArguments) {
        val fromType = requireNotNull(fromProperties[name]) { "Can't find matching property $name in $sourceClassType" }
        require(fromType == toType) { "Source property $name type $fromType doesn't match target type $toType" }
    }
}

context(CodeBlock.Builder)
private fun KFunction<*>.addArguments(parameterName: String) {
    parameters.dropLast(1).forEach { add("$parameterName.%N, ", it.name) }
    add("$parameterName.%N", parameters.last().name)
}
