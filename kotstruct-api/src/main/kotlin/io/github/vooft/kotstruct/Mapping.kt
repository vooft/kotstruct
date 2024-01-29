package io.github.vooft.kotstruct

import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf

data class TypeMapping<Source : Any, Target : Any>(val from: KType, val to: KType, val mapper: Function1<Source, Target>) {
    companion object {
        inline fun <reified Source : Any, reified Target : Any> create(noinline mapper: Function1<Source, Target>) = TypeMapping(
            from = typeOf<Source>(),
            to = typeOf<Target>(),
            mapper = mapper
        )
    }
}

data class FieldMapping<Source : Any, Target : Any>(
    val from: KType,
    val to: KType,
    val fromPath: List<KProperty<*>>,
    val toPath: List<KProperty<*>>,
    val mapper: Function1<Source, Target>
)

data class FactoryMapping<Target : Any>(val to: KType, val factory: KFunction<Target>)

data class MappingsDefinitions(
    val typeMappings: List<TypeMapping<*, *>>,
    val factoryMappings: List<FactoryMapping<*>>,
    val fieldMappings: List<FieldMapping<*, *>>,
) {
    fun findTypeMapping(source: KType, target: KType) = typeMappings.find { it.from == source && it.to == target }

    fun findFactoryMapping(target: KType) = factoryMappings.find { it.to == target }

    companion object {
        val EMPTY = MappingsDefinitions(
            typeMappings = emptyList(),
            factoryMappings = emptyList(),
            fieldMappings = emptyList(),
        )
    }
}
