package io.github.vooft.kotstruct.ksp

import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import io.github.vooft.kotstruct.KotStructCustomConstructor
import io.github.vooft.kotstruct.KotStructMapper

interface KotStructMapperGenerator {
    fun generateFor(mapperClass: KSClassDeclaration)
}

class KotStructMapperGeneratorImpl(private val codeGenerator: CodeGenerator) : KotStructMapperGenerator {
    override fun generateFor(mapperClass: KSClassDeclaration) {
        val mapperSupertype = requireNotNull(mapperClass.findKotStructMapperSupertype()) {
            "Class ${mapperClass.qualifiedName} must inherit from ${KotStructMapper::class}"
        }

        val fromClass = mapperSupertype.arguments.first().type!!.resolve().declaration as KSClassDeclaration
        val toClass = mapperSupertype.arguments.last().type!!.resolve().declaration as KSClassDeclaration

        val fromProperties = fromClass.declarations
            .filterIsInstance<KSPropertyDeclaration>()
            .associate { it.simpleName.asString() to it.type.resolve() }

        // TODO: handle custom-defined constructor
        val customConstructor = mapperClass.declarations
            .filterIsInstance<KSFunctionDeclaration>()
            .singleOrNull { function ->
                function.annotations.any { it.annotationType.qualifiedName == KotStructCustomConstructor::class.qualifiedName }
            }

        if (customConstructor != null) {
            require(customConstructor.returnType?.resolve()?.declaration == toClass) {
                "Custom constructor must return $toClass, but returns ${customConstructor.returnType}"
            }
        }

        val toTypeConstructor = customConstructor
            ?: requireNotNull(toClass.primaryConstructor) { "Primary constructor must be present in $toClass" }

        val toArguments = toTypeConstructor.parameters.associate { it.name!!.asString() to it.type.resolve() }

        for ((name, toType) in toArguments) {
            val fromType = requireNotNull(fromProperties[name]) { "Can't find matching property $name in $fromClass" }
            require(fromType == toType) { "Source property type $fromType doesn't match target type $toType" }
        }

        val genertedClassName = "$GENERATED_PREFIX${mapperClass.simpleName.asString()}"
        FileSpec.builder(mapperClass.packageName.asString(), genertedClassName)
            .addType(
                TypeSpec.classBuilder(genertedClassName)
                    .addSuperinterface(mapperClass.toClassName())
                    .addFunction(FunSpec.builder("map")
                        .addParameter("from", fromClass.toClassName())
                        .addModifiers(KModifier.OVERRIDE)
                        .addCode(CodeBlock.builder()
                            .apply {
                                when(toTypeConstructor.isConstructor()) {
                                    true -> add("return %T(", toClass.toClassName())
                                    false -> add("return %N(", toTypeConstructor.simpleName.asString())
                                }
                            }
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
