package io.github.vooft.kotstruct

interface KotStructMapper<From: Any, To: Any> {
    fun map(from: From): To
}

@Target(AnnotationTarget.FUNCTION)
annotation class KotStructCustomConstructor
