package io.github.vooft.kotstruct.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
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
                    .addAnnotation(AnnotationSpec.builder(Suppress::class).addMember("\"detekt.all\", \"unchecked_cast\"").build())
                    .addSuperinterface(mapperClass.asTypeName())
                    .generateMemberFunctions(mapperClass)
                    .build()
            )
            .addImport("kotlin.reflect", "typeOf")
            .build()
            .apply { writeTo(System.out) }
            .writeTo(codeGenerator, false)
    }

    private fun TypeSpec.Builder.generateMemberFunctions(mapperClass: KClass<out KotStructMapper>): TypeSpec.Builder {
        val memberFunctions = mapperClass.declaredMemberFunctions
        logger.info("Found at class $mapperClass methods: $memberFunctions")

        val descriptorClass = mapperClass.findAnnotation<KotStructDescribedBy>()?.descriptor
        logger.info("Found descriptor $descriptorClass for mapper $mapperClass")

        val descriptor = descriptorClass?.objectInstance ?: KotStructDescriptor.EMPTY

        for (memberFunction in memberFunctions) {
            when {
                !memberFunction.isAbstract ->
                    logger.info("Method ${memberFunction.name} is not abstract, ignoring")

                memberFunction.returnType.classifier == Unit::class ->
                    logger.info("Method ${memberFunction.name} doesn't return anything, nothing to map to")

                memberFunction.parameters.isEmpty() ->
                    logger.info("Method ${memberFunction.name} doesn't accept any parameters, nothing to map from")

                memberFunction.parameters.size > 2 ->
                    logger.info("Method ${memberFunction.name} accepts more than 2 parameters (including self), ignoring")

                else -> {
                    logger.info("Processing method $memberFunction")
                    val parameter = memberFunction.parameters.last()

                    val session = KotStructGeneratorSession(
                        logger = logger,
                        sourceClassType = parameter.type,
                        targetClassType = memberFunction.returnType,
                        descriptor = descriptor
                    )

                    // TODO: group all generated types under a single object
                    val generatedType = session.generateMapperObject()
                    addType(generatedType)

                    addFunction(
                        FunSpec.builder(memberFunction.name)
                            .addModifiers(KModifier.OVERRIDE)
                            .addParameter(parameter.name!!, parameter.type.asTypeName())
                            .addCode("return %L(%L)", generatedType.name, parameter.name!!)
                            .returns(memberFunction.returnType.asTypeName())
                            .build()
                    )
                }
            }
        }

        return this
    }

//    private fun generateMappersHolder(descriptor: KotStructDescriptor): TypeSpec {
//        return TypeSpec.objectBuilder("GeneratedMappersHolder")
//
//            .build()
//    }
}
