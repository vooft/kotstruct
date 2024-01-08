package io.github.vooft.kotstruct.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import io.github.vooft.kotstruct.KotStructMapper

interface KotStructMapperGenerator {
    fun generateFor(clazz: KSClassDeclaration)
}

class KotStructMapperGeneratorImpl(private val codeGenerator: CodeGenerator) : KotStructMapperGenerator {
    override fun generateFor(clazz: KSClassDeclaration) {
        val mapperSupertype = requireNotNull(clazz.findKotStructMapperSupertype()) {
            "Class ${clazz.qualifiedName} must inherit from ${KotStructMapper::class}"
        }

        val fromClass = mapperSupertype.arguments.first().type!!.resolve().declaration as KSClassDeclaration
        val toClass = mapperSupertype.arguments.last().type!!.resolve().declaration as KSClassDeclaration

        val fromProperties = fromClass.declarations
            .filterIsInstance<KSPropertyDeclaration>()
            .associate { it.simpleName.asString() to it.type.resolve() }

        // TODO: handle custom-defined constructor
        val toTypeConstructor = requireNotNull(toClass.primaryConstructor) { "Primary constructor must be present in $toClass" }
        val toArguments = toTypeConstructor.parameters.associate { it.name!!.asString() to it.type.resolve() }

        for ((name, toType) in toArguments) {
            val fromType = requireNotNull(fromProperties[name]) { "Can't find matching property $name in $fromClass" }
            require(fromType == toType) { "Source property type $fromType doesn't match target type $toType" }
        }

        val genertedClassName = "$GENERATED_PREFIX${clazz.simpleName.asString()}"
        FileSpec.builder(clazz.packageName.asString(), genertedClassName)
            .addType(
                TypeSpec.classBuilder(genertedClassName)
                    .addSuperinterface(clazz.toClassName())
                    .addProperty(
                        PropertySpec.builder("constructor", toTypeConstructor.toKFunction(), KModifier.OVERRIDE)
                            .initializer("::%T", toClass.toClassName())
                            .build()
                    )
                    .addFunction(FunSpec.builder("map")
                        .addParameter("from", fromClass.toClassName())
                        .addModifiers(KModifier.OVERRIDE)
                        .addCode(CodeBlock.builder()
                            .add("return constructor(")
                            .apply {
                                // TODO: improve
                                for (p in toTypeConstructor.parameters) {
                                    add("from.%N, ", p.name!!.asString())
                                }
                            }
                            .add(")")
                            .build())
                        .returns(toClass.toClassName())
                        .build()
                    )
                    .build()
            )
            .build()
            .writeTo(codeGenerator, false)
    }
}

internal const val GENERATED_PREFIX = "KotStructGenerated"

private fun KSFunctionDeclaration.toKFunction() = ClassName("kotlin.reflect", "KFunction" + parameters.size)
    .parameterizedBy(parameters.map { it.type.resolve().toTypeName() } + returnType!!.toTypeName())
