package io.github.vooft.kotstruct

import kotlin.reflect.KFunction

interface KotStructMapper<From: Any, To: Any> {
    val constructor: KFunction<To>
    fun map(from: From): To
}
