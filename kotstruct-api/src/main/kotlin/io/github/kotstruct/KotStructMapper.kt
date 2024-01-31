package io.github.kotstruct

import io.github.kotstruct.dsl.KotStructDescriptorDsl
import kotlin.reflect.KClass

interface KotStructMapper

interface KotStructDescriptor {
    // keep key as string to simplify codegen
    val mappings: MappingsDefinitions

    companion object {
        val EMPTY: KotStructDescriptor = object : KotStructDescriptor {
            override val mappings = MappingsDefinitions()
        }

        fun kotStruct(block: KotStructDescriptorDsl.() -> Unit): KotStructDescriptor {
            val dsl = KotStructDescriptorDsl()
            dsl.block()

            // have a separate value to avoid capturing of the dsl
            val mappings = dsl.build()
            return object : KotStructDescriptor {
                override val mappings = mappings
            }
        }
    }
}

@Target(AnnotationTarget.CLASS)
annotation class KotStructDescribedBy(val descriptor: KClass<out KotStructDescriptor>)


