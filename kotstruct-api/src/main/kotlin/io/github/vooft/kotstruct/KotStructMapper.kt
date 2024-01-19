package io.github.vooft.kotstruct

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType

interface KotStructMapper

interface KotStructDescriptor {
    val mappings: Map<String, MappingDefinition<*>>

    companion object {
        val EMPTY_CLASS: KClass<out KotStructDescriptor> = EmptyKotStructDescriptor::class
    }
}

data class MappingDefinition<Target: Any>(
    val factory: KFunction<Target>,
    val mapper: Function<Target>
) {
    companion object {
        fun <Target: Any> of(factory: KFunction<Target>, mapper: Function<Target>) =
            MappingDefinition(factory, mapper)
    }
}

fun KType.mappingInto(target: KType) = toString() + "____" + target.toString()

internal object EmptyKotStructDescriptor : KotStructDescriptor {
    override val mappings: Map<String, MappingDefinition<*>> = mapOf()
}

@Target(AnnotationTarget.CLASS)
annotation class KotStructDescribedBy(val descriptor: KClass<out KotStructDescriptor>)


