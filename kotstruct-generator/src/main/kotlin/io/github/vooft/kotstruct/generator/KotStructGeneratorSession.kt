package io.github.vooft.kotstruct.generator

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asTypeName
import io.github.vooft.kotstruct.KotStructDescribedBy
import io.github.vooft.kotstruct.KotStructDescriptor
import io.github.vooft.kotstruct.mappingInto
import io.github.vooft.kotstruct.primaryConstructor
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure

class KotStructGeneratorSession(private val method: KFunction<*>, private val descriptorClass: KClass<out KotStructDescriptor>) {

    private val sourceClassType = run {
        // first argument is the receiver, second is actual argument
        require(method.parameters.size == 2) { "Mapping method $method must have exactly 1 argument" }
        method.parameters.last().type
    }
    private val targetClassType = method.returnType

    private val fromProperties = sourceClassType.jvmErasure.memberProperties.associate { it.name to it.returnType }

    fun generateMethod(): FunSpec {
        val mappingId = sourceClassType.mappingInto(targetClassType)
        val customMapping = run {
            val descriptor = requireNotNull(descriptorClass.objectInstance) {
                "@${KotStructDescribedBy::class.simpleName} must reference an object, but $descriptorClass is not"
            }

            descriptor.mappings[mappingId]
        }

        val toTypeConstructor = customMapping?.factory ?: targetClassType.primaryConstructor
        val kfunctionClassName = ClassName("kotlin.reflect", "KFunction" + toTypeConstructor.parameters.size)
            .parameterizedBy(toTypeConstructor.parameters.map { it.type.asTypeName() } + toTypeConstructor.returnType.asTypeName())

        val toArguments = toTypeConstructor.parameters.associate { it.name!! to it.type }

        for ((name, toType) in toArguments) {
            val fromType = requireNotNull(fromProperties[name]) { "Can't find matching property $name in $sourceClassType" }
            require(fromType == toType) { "Source property $name type $fromType doesn't match target type $toType" }
        }

        return FunSpec.builder("map")
            .addAnnotation(AnnotationSpec.builder(Suppress::class).addMember("\"unchecked\"").build())
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("src", sourceClassType.asTypeName())
            .addCode(
                CodeBlock.builder()
                    .apply {
                        when (customMapping) {
                            null -> add("return %T(", targetClassType.asTypeName())
                            else -> add("return (%T.mappings.getValue(\"$mappingId\").factory as %T)(", descriptorClass, kfunctionClassName)
                        }
                    }
                    .apply {
                        toTypeConstructor.parameters.dropLast(1)
                            .forEach { add("src.%N, ", it.name) }
                        add("src.%N", toTypeConstructor.parameters.last().name)
                    }
                    .add(")")
                    .build())
            .returns(targetClassType.asTypeName())
            .build()
    }
}
