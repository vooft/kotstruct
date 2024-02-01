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

sealed interface FieldMapping<Target> {
    val to: KType
    val toPath: List<KProperty<*>>
}

data class FactoryToFieldMapping<Target>(
    override val to: KType,
    override val toPath: List<KProperty<*>>,
    val returnType: KType,
    val factory: () -> Target
): FieldMapping<Target>

data class FieldToFieldMapping<Target>(
    val from: KType,
    override val to: KType,
    val fromPath: List<KProperty<*>>,
    override val toPath: List<KProperty<*>>
) : FieldMapping<Target> {
    init {
        // TODO: validate fromPath and toPath
    }
    companion object {
        inline fun <reified Source : Any, reified Target : Any> create(
            fromPath: List<KProperty<*>>,
            toPath: List<KProperty<*>>
        ): FieldToFieldMapping<Target> {
            return FieldToFieldMapping(from = typeOf<Source>(), to = typeOf<Target>(), fromPath = fromPath, toPath = toPath)
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
    val fieldMappings: List<FieldMapping<*>> = emptyList(),
) {
    companion object {
        val EMPTY = MappingsDefinitions()
    }
}
