package io.github.vooft.kotstruct

import kotlin.reflect.KClass
import kotlin.reflect.KType

interface KotStructMapper

interface MappingTypes {
    val source: List<KType>
    val target: KType
}

interface KotStructDescriptor {
    // keep key as string to simplify codegen
    val mappings: Map<String, Mapping<*>>

    companion object {
        val EMPTY_CLASS: KClass<out KotStructDescriptor> = EmptyKotStructDescriptor::class
    }
}

internal data class MappingTypesImpl(override val source: List<KType>, override val target: KType) : MappingTypes

// keep key as string to simplify codegen
//fun KType.mappingInto(target: KType): MappingTypes = MappingTypesImpl(listOf(this), target)
fun KType.mappingInto(target: KType) = toString() + "____" + target.toString()

internal object EmptyKotStructDescriptor : KotStructDescriptor {
    override val mappings: Map<String, Mapping<*>> = mapOf()
}

@Target(AnnotationTarget.CLASS)
annotation class KotStructDescribedBy(val descriptor: KClass<out KotStructDescriptor>)


