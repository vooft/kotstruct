package io.github.kotstruct

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
    val mapper: Function1<Source, Target>?
) {
    init {
        // TODO: validate fromPath and toPath
    }
    companion object {
        inline fun <reified Source : Any, reified Target : Any> create(
            fromPath: List<KProperty<*>>,
            toPath: List<KProperty<*>>
        ): FieldMapping<Source, Target> {
            return FieldMapping(from = typeOf<Source>(), to = typeOf<Target>(), fromPath = fromPath, toPath = toPath, mapper = null)
        }
    }
}

data class FactoryMapping<Target : Any>(val to: KType, val factory: KFunction<Target>) {
    companion object {
        inline fun <reified Target : Any> create(factory: KFunction<Target>) = FactoryMapping(
            to = typeOf<Target>(),
            factory = factory
        )
    }
}

data class MappingsDefinitions(
    val typeMappings: List<TypeMapping<*, *>> = emptyList(),
    val factoryMappings: List<FactoryMapping<*>> = emptyList(),
    val fieldMappings: List<FieldMapping<*, *>> = emptyList(),
) {
    companion object {
        val EMPTY = MappingsDefinitions()
    }
}
