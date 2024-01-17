package io.github.vooft.kotstruct

import kotlin.reflect.KClass
import kotlin.reflect.KFunction

interface KotStructMapper<Source: Any, Target: Any> {
    fun map(src: Source): Target
}

interface KotStructDescriptorValue<out Target: Any> {

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

    val imports: List<KotStructMapper<*, *>> get() = listOf()
}

class KotStructNotDefinedException : RuntimeException()

@Target(AnnotationTarget.CLASS)
annotation class KotStructDescriptor(val descriptor: KClass<out KotStructDescriptorValue<*>>)


