package io.github.vooft.kotstruct

import kotlin.reflect.KClass
import kotlin.reflect.KFunction

interface KotStructMapper<Source: Any, Target: Any> {
    fun map(src: Source): Target
}

interface KotStructDescriptor<out Target: Any> {
    val constructor: KFunction<Target> get() = throw KotStructNotDefinedException()

    val imports: List<KotStructMapper<*, *>> get() = listOf()
}

class KotStructNotDefinedException : RuntimeException()

@Target(AnnotationTarget.CLASS)
annotation class KotStructDescriptorClass(val descriptor: KClass<out KotStructDescriptor<*>>)


