package io.github.kotstruct.dsl

import io.github.kotstruct.FactoryMapping
import io.github.kotstruct.FieldMapping
import io.github.kotstruct.MappingsDefinitions
import io.github.kotstruct.TypeMapping
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class KotStructDescriptorDsl {

    private val typeMappings = mutableListOf<TypeMapping<*, *>>()
    private val factoryMappings = mutableListOf<FactoryMapping<*>>()
    private val fieldMappings = mutableListOf<FieldMapping<*, *>>()
    fun <Source: Any, Target: Any> mapperFor(from: KType, to: KType, mapper: (Source) -> Target) {
        // TODO: validate only one from-to pair
        typeMappings.add(TypeMapping(from, to, mapper))
    }

    inline fun <reified Source: Any, reified Target: Any> mapperFor(noinline mapper: (Source) -> Target) {
        mapperFor(typeOf<Source>(), typeOf<Target>(), mapper)
    }

    fun <Source: Any, Target: Any> mappingFor(source: KType, target: KType, block: MappingDsl<Source, Target>.() -> Unit) {
        val dsl = MappingDsl<Source, Target>(source = source, target = target)
        dsl.block()
        fieldMappings.addAll(dsl.build())
    }

    inline fun <reified Source: Any, reified Target: Any> mappingFor(noinline block: MappingDsl<Source, Target>.() -> Unit) {
        mappingFor(typeOf<Source>(), typeOf<Target>(), block)
    }

    fun <Target: Any> factoryFor(target: KType, block: () -> KFunction<Target>) {
        factoryMappings.add(FactoryMapping(target, block()))
    }

    inline fun <reified Target: Any> factoryFor(noinline block: () -> KFunction<Target>) {
        factoryFor(typeOf<Target>(), block)
    }

    internal fun build(): MappingsDefinitions {
        // TODO: validate that all field mappers with different types have corresponding type mappings

        return MappingsDefinitions(
            typeMappings = typeMappings.toList(),
            factoryMappings = factoryMappings.toList(),
            fieldMappings = fieldMappings.toList()
        )
    }
}

class MappingDsl<Source: Any, Target: Any>(private val source: KType, private val target: KType) {

    private val fieldMappings = mutableListOf<FieldMapping<Source, Target>>()

    fun <Return> map(block: PropertiesPathDsl<Source>.() -> KProperty<Return>): PropertiesPath<Return> {
        val dsl = PropertiesPathDsl<Source>()
        val latest = dsl.block()
        return PropertiesPath(dsl.build() + latest)
    }


    infix fun <R1, R2> PropertiesPath<R1>.into(block: PropertiesPathDsl<Target>.() -> KProperty1<*, R2>) {
        val dsl = PropertiesPathDsl<Target>()
        val latest = dsl.block()

        fieldMappings.add(FieldMapping(
            from = source,
            to = target,
            fromPath = path,
            toPath = dsl.build() + latest,
            mapper = null
        ))
    }

    internal fun build(): List<FieldMapping<Source, Target>> {
        println(source)
        println(target)

        return fieldMappings
    }
}

class PropertiesPathDsl<Source: Any> {

    private val currentPath = mutableListOf<KProperty<*>>()

    operator fun <R1, R2> KProperty1<Source, R1>.div(another: KProperty1<R1, R2>): KProperty1<R1, R2> {
        currentPath.add(this)
        return another
    }

    internal fun build(): List<KProperty<*>> {
        return currentPath.toList()
    }
}

data class PropertiesPath<T>(val path: List<KProperty<*>>)
