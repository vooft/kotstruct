package io.github.vooft.kotstruct

import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.typeOf

// definitions

sealed interface Mapping<Target : Any> {
    val targetType: KType

    sealed interface CustomFactoryMapping<Target : Any> : Mapping<Target> {
        val factory: KFunction<Target>
    }

    sealed interface CustomMapperMapping<Target : Any> : Mapping<Target> {
        val mapper: Function<Target>
    }

    companion object {
        inline fun <reified Target: Any> customFactory(factory: KFunction<Target>): CustomFactoryMapping<Target>
            = MappingImplementation.CustomFactoryMappingImpl(typeOf<Target>(), factory)

        inline fun <reified Source: Any, reified Target: Any> customMapper(noinline mapper: (Source) -> Target): CustomMapperMapping<Target>
                = MappingImplementation.CustomMapper1MappingImpl(typeOf<Source>(), typeOf<Target>(), mapper)
    }
}

object MappingImplementation {
    data class CustomFactoryMappingImpl<Target : Any>(
        override val targetType: KType,
        override val factory: KFunction<Target>
    ) : Mapping.CustomFactoryMapping<Target>

    data class CustomMapper1MappingImpl<Source: Any, Target : Any>(
        val sourceType: KType,
        override val targetType: KType,
        override val mapper: (Source) -> Target
    ) : Mapping.CustomMapperMapping<Target>
}
