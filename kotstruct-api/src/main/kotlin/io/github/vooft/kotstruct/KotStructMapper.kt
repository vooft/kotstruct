package io.github.vooft.kotstruct

import kotlin.reflect.KFunction

interface KotStructMapper<From: Any, To: Any> {
    val constructor: KFunction<To> get() = throw KotStructNotDefinedException()

    val imports: List<KotStructMapper<*, *>> get() = listOf()
}

class KotStructNotDefinedException : RuntimeException()

@Target(AnnotationTarget.FUNCTION)
annotation class KotStructCustomConstructor
