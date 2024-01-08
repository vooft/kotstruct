package io.github.vooft.kotstruct.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import io.github.vooft.kotstruct.KotStructMapper

private val MAPPER_CLASS_NAME = KotStructMapper::class.qualifiedName!!
fun KSClassDeclaration.findKotStructMapperSupertype(): KSType? {
    return superTypes.firstOrNull { it.qualifiedName == MAPPER_CLASS_NAME }?.resolve()
}

val KSTypeReference.qualifiedName: String get() = requireNotNull(resolve().declaration.qualifiedName) {
    "Qualified name is not present at KSTypeReference $this"
}.asString()
