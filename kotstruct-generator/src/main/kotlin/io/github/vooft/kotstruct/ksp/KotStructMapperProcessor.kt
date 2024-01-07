package io.github.vooft.kotstruct.ksp

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

class KotStructMapperProcessor(private val generator: KotStructMapperGenerator) : SymbolProcessor {
    private val generatedFiles = mutableSetOf<String>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getNewFiles()
            .filterNot { generatedFiles.contains(it.filePath) }
            .flatMap { it.declarations }
            .filterIsInstance<KSClassDeclaration>()
            .filter { isKotStructMapper(it) }
            .forEach { generator.generateFor(it) }

        return emptyList()
    }

    private fun isKotStructMapper(clazz: KSClassDeclaration): Boolean {
        if (clazz.classKind != ClassKind.INTERFACE) {
            return false
        }

        return clazz.findKotStructMapperSupertype() != null
    }
}

class KotStructMapperProcessorProvider(
    private val defaultGenerator: KotStructMapperGenerator? = null
) : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val generator = defaultGenerator ?: KotStructMapperGeneratorImpl(environment.codeGenerator)
        return KotStructMapperProcessor(generator)
    }

}
