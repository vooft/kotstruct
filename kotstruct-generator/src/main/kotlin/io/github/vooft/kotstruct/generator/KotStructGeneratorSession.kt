package io.github.vooft.kotstruct.generator

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.asTypeName
import io.github.vooft.kotstruct.KotStructDescriptor
import io.github.vooft.kotstruct.KotStructNotDefinedException
import io.github.vooft.kotstruct.primaryConstructor
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.jvmErasure

class KotStructGeneratorSession(private val method: KFunction<*>, private val descriptorClass: KClass<out KotStructDescriptor<Any>>) {

    private val sourceClassType = run {
        // first argument is the receiver, second is actual argument
        require(method.parameters.size == 2) { "Mapping method $method must have exactly 1 argument" }
        method.parameters.last().type
    }
    private val targetClassType = method.returnType

    private val fromProperties = sourceClassType.jvmErasure.memberProperties.associate { it.name to it.returnType }

    fun generateMethod(): FunSpec {
        val customConstructor = descriptorClass.let {
            val descriptor = requireNotNull(it.objectInstance) {
                "KotStructDescriptor must reference an object, but $it is not"
            }

            @Suppress("SwallowedException")
            try {
                require(descriptor.constructor.returnType == targetClassType) {
                    "Custom constructor must return $targetClassType, but returns ${descriptor.constructor.returnType}"
                }

                descriptor.constructor
            } catch (e: KotStructNotDefinedException) {
                // ignore
                null
            }
        }

        val toTypeConstructor = customConstructor ?: targetClassType.primaryConstructor

        val toArguments = toTypeConstructor.parameters.associate { it.name!! to it.type }

        for ((name, toType) in toArguments) {
            val fromType = requireNotNull(fromProperties[name]) { "Can't find matching property $name in $sourceClassType" }
            require(fromType == toType) { "Source property $name type $fromType doesn't match target type $toType" }
        }

        return FunSpec.builder("map")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("src", sourceClassType.asTypeName())
            .addCode(
                CodeBlock.builder()
                    .apply {
                        when (customConstructor) {
                            null -> add("return %T(", targetClassType.asTypeName())
                            else -> add("return %T.constructor(", descriptorClass)
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
