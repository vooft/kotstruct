package io.github.vooft.kotstruct.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import io.github.vooft.kotstruct.KotStructMapper

private val MAPPER_CLASS_NAME = KotStructMapper::class.qualifiedName!!
fun KSClassDeclaration.findKotStructMapperSupertype(): KSType? {
    return superTypes
        .map { it.resolve() }
        .firstOrNull { it.declaration.qualifiedName?.asString() == MAPPER_CLASS_NAME }
}
