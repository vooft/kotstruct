package io.github.vooft.kotstruct.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration
import io.github.vooft.kotstruct.KotStructMapper

interface KotStructMapperGenerator {
    fun generateFor(clazz: KSClassDeclaration)
}

class KotStructMapperGeneratorImpl(private val codeGenerator: CodeGenerator) : KotStructMapperGenerator {
    override fun generateFor(clazz: KSClassDeclaration) {
        val mapperSupertype = requireNotNull(clazz.findKotStructMapperSupertype()) {
            "Class ${clazz.qualifiedName} must inherit from ${KotStructMapper::class}"
        }

        val fromType = mapperSupertype.arguments.first().type!!.resolve()
        val toType = mapperSupertype.arguments.last().type!!.resolve()

        println(fromType)
        println(toType)
        println(codeGenerator)
    }

}
