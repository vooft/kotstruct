package io.github.vooft.kotstruct

import java.security.MessageDigest
import java.util.HexFormat
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor

fun KClass<out KotStructMapper<*, *>>.findKotStructMapperSupertype(): KType {
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
