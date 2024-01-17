package io.github.vooft.kotstruct

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import java.security.MessageDigest
import java.util.HexFormat
import kotlin.reflect.KClass
import kotlin.reflect.KType


private val MAPPER_CLASS_NAME = KotStructMapper::class.qualifiedName!!
fun KSClassDeclaration.findKotStructMapperSupertype(): KSType? {
    return superTypes.firstOrNull { it.qualifiedName == MAPPER_CLASS_NAME }?.resolve()
}

fun KClass<out KotStructMapper<*, *>>.findKotStructMapperSupertype(): KType {
    return supertypes.single { it.classifier == KotStructMapper::class }
}

val KClass<*>.packageName: String get() = qualifiedName!!.split(".").dropLast(1).joinToString(".")

val KSTypeReference.qualifiedName: String get() = requireNotNull(resolve().declaration.qualifiedName) {
    "Qualified name is not present at KSTypeReference $this"
}.asString()

private val shaMessageDigest = ThreadLocal.withInitial { MessageDigest.getInstance("SHA-1") }
internal fun String.sha1(): String {
    val messageDigest = shaMessageDigest.get()
    val textBytes = toByteArray()
    messageDigest.update(textBytes, 0, textBytes.size)
    return HexFormat.of().formatHex(messageDigest.digest())
}

internal const val GENERATED_PREFIX = "KotStructGenerated"
internal const val GENERATED_PACKAGE = "io.github.vooft.kotstruct"
