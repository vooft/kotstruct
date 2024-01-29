package io.github.vooft.kotstruct

import kotlin.reflect.KClass
import kotlin.reflect.KType

interface KotStructMapper

interface KotStructDescriptor {
    // keep key as string to simplify codegen
    val mappings: MappingsDefinitions

    companion object {
        val EMPTY_CLASS: KClass<out KotStructDescriptor> = EmptyKotStructDescriptor::class
        val EMPTY: KotStructDescriptor = EmptyKotStructDescriptor
    }
}

// keep key as string to simplify codegen
//fun KType.mappingInto(target: KType): MappingTypes = MappingTypesImpl(listOf(this), target)
fun KType.mappingInto(target: KType) = toString() + "____" + target.toString()

internal object EmptyKotStructDescriptor : KotStructDescriptor {
    override val mappings = MappingsDefinitions.EMPTY
}

@Target(AnnotationTarget.CLASS)
annotation class KotStructDescribedBy(val descriptor: KClass<out KotStructDescriptor>)


