package io.github.vooft.kotstruct.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import io.github.vooft.kotstruct.KotStructMapper
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
