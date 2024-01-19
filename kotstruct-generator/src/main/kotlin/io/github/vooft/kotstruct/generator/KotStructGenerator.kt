package io.github.vooft.kotstruct.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import io.github.vooft.kotstruct.GENERATED_PACKAGE
import io.github.vooft.kotstruct.GENERATED_PREFIX
import io.github.vooft.kotstruct.KotStructDescribedBy
import io.github.vooft.kotstruct.KotStructDescriptor
import io.github.vooft.kotstruct.KotStructMapper
import io.github.vooft.kotstruct.sha1
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.findAnnotation

class KotStructGenerator(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) {
    fun process(mapperClass: KClass<out KotStructMapper>) {
        // TODO: use sha1
        val generatedClassName = "$GENERATED_PREFIX${mapperClass.simpleName}"
        println("generatedClassName: $generatedClassName, ${mapperClass.toString().sha1()}")
        FileSpec.builder(GENERATED_PACKAGE, generatedClassName)
            .addType(
                TypeSpec.classBuilder(generatedClassName)
                    .addSuperinterface(mapperClass.asTypeName())
                    .generateMemberFunctions(mapperClass)
                    .build()
            )
            .build()
            .apply {writeTo(System.out) }
            .writeTo(codeGenerator, false)
    }

    private fun TypeSpec.Builder.generateMemberFunctions(mapperClass: KClass<out KotStructMapper>): TypeSpec.Builder {
        val memberFunctions = mapperClass.declaredMemberFunctions


        logger.info("Found at class $mapperClass methods: $memberFunctions")
        val descriptorClass = mapperClass.findAnnotation<KotStructDescribedBy>()?.descriptor

        for (memberFunction in memberFunctions) {
            when {
                !memberFunction.isAbstract ->
                    logger.info("Method ${memberFunction.name} is not abstract, ignoring")

                memberFunction.returnType.classifier == Unit::class ->
                    logger.info("Method ${memberFunction.name} doesn't return anything, nothing to map to")

                memberFunction.parameters.isEmpty() ->
                    logger.info("Method ${memberFunction.name} doesn't accept any parameters, nothing to map from")

                else -> {
                    logger.info("Processing method $memberFunction")
                    val session = KotStructGeneratorSession(memberFunction, descriptorClass ?: KotStructDescriptor.EMPTY_CLASS)
                    addFunction(session.generateMethod())
                }
            }
        }

        return this
    }
}
