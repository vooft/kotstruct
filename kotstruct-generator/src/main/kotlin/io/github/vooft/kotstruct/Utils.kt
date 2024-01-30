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

internal fun TypeMappingDefinition.toFunctionTypeName() = Function1::class.asClassName()
    .parameterizedBy(mapping.from.asTypeName(), mapping.to.asTypeName())

internal data class TypeMappingDefinition(val index: Int, val identifier: String, val mapping: TypeMapping<*, *>)
internal data class FactoryMappingDefinition(val index: Int, val identifier: String, val mapping: FactoryMapping<*>)

internal fun KFunction<*>.toParametrizedTypeName(): ParameterizedTypeName {
    val kFunction = "KFunction" + parameters.size
    return ClassName("kotlin.reflect", kFunction)
        .parameterizedBy(
            // last kfunction parameter is the return type
            parameters.map { it.type.asTypeName() } + returnType.asTypeName()
        )
}

internal inline fun <T, K, V> Iterable<T>.associateIndexed(transform: (Int, T) -> Pair<K, V>) = buildMap<K, V> {
    forEachIndexed { index, t ->
        val pair = transform(index, t)
        put(pair.first, pair.second)
    }
}

