package io.github.vooft.kotstruct

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import java.security.MessageDigest
import java.util.HexFormat
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor

fun KClass<out KotStructMapper>.findKotStructMapperSupertype(): KType {
    return supertypes.single { it.classifier == KotStructMapper::class }
}

val KClass<*>.packageName: String get() = qualifiedName!!.split(".").dropLast(1).joinToString(".")

val KType.primaryConstructor: KFunction<Any> get() = requireNotNull((classifier as KClass<*>).primaryConstructor) {
    "Primary constructor must be present in $this"
}

private val shaMessageDigest = ThreadLocal.withInitial { MessageDigest.getInstance("SHA-1") }
internal fun String.sha1(): String {
    val messageDigest = shaMessageDigest.get()
    val textBytes = toByteArray()
    messageDigest.update(textBytes, 0, textBytes.size)
    return HexFormat.of().formatHex(messageDigest.digest())
}

internal const val GENERATED_PREFIX = "KotStructGenerated"
internal const val GENERATED_PACKAGE = "io.github.vooft.kotstruct"

internal val IDENTIFIER_COUNTER = AtomicInteger()

internal data class TypePair(val from: KType, val to: KType)

internal fun TypePair.toFunctionTypeName() = Function1::class.asClassName()
    .parameterizedBy(from.asTypeName(), to.asTypeName())

internal data class TypeMappingDefinition(val identifier: String)
internal data class FactoryMappingDefinition(val identifier: String, val factory: KFunction<Any>)

internal fun KFunction<*>.toTypeName(): ParameterizedTypeName {
    val kFunction = "KFunction" + parameters.size
    return ClassName("kotlin.reflect", kFunction)
        .parameterizedBy(
            // last kfunction parameter is the return type
            parameters.map { it.type.asTypeName() } + returnType.asTypeName()
        )
}
