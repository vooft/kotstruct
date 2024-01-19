package io.github.vooft.kotstruct

import kotlin.reflect.KClass
import kotlin.reflect.KFunction

interface KotStructMapper

interface KotStructDescriptor<out Target: Any> {

    /**
     * Custom constructor for the target class
     *
     * In order to use a secondary constructor, must specify correct KFunctionN, for example:
     * ```
     * class TargetDto(val id: String, val name: String) {
     *     constructor(id: String) : this(id, "default value")
     * }
     *
     * object MyMapperDescriptor : KotStructDescriptor<TargetDto> {
     *     override val constructor: KFunction2<String, String, TargetDto> = ::TargetDto
     * }
     * ```
     */
    val constructor: KFunction<Target> get() = throw KotStructNotDefinedException()

    companion object {
        val EMPTY_CLASS: KClass<out KotStructDescriptor<*>> = EmptyKotStructDescriptor::class
    }
}

internal object EmptyKotStructDescriptor : KotStructDescriptor<Any>

class KotStructNotDefinedException : RuntimeException()

@Target(AnnotationTarget.CLASS)
annotation class KotStructMapperDescriptor(val descriptor: KClass<out KotStructDescriptor<*>>)


