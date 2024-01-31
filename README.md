# KotStruct

## Summary
[MapStruct](https://mapstruct.org/)-like library for Kotlin for compile-time generation of mappers.

It is type-safe and utilizes Kotlin DSL to describe the mapper, instead of annotations.

This library is mostly designed for mapping into immutable data structures, like data classes.

## Supported features

* Direct type mappers. For example, `String` -> `UUID`.
* Factory definition. By default, library uses primary constructor to build an object, it is possible to override this.
* Type-safe field mapping. It is possible to specify which field should be used as a source for another field, also takes in account existing direct type mappers.

## Mapper Example

```kotlin
@KotStructDescribedBy(FieldMappingMapperDescriptor::class)
interface FieldMappingMapper : KotStructMapper {

    // mapping method must be abstract
    fun map(source: SourceDto): TargetDto

    data class SourceDto(val srcId: String, val nested: Nested, val toChild: String) {
        data class Nested(val srcUuid: String, val toParent: String)
    }

    data class TargetDto(val id: String, val nested: Nested, val fromChild: String) {
        data class Nested(val uuid: UUID, val fromParent: String)
    }
}

object FieldMappingMapperDescriptor : KotStructDescriptor by kotStruct({
    // defining a direct type mapper String -> UUID
    mapperFor<String, UUID> { UUID.fromString(it) }
    
    mappingFor<SourceDto, TargetDto> {
        // fields can be mapped on the same level with different names
        map { SourceDto::srcId } into { TargetDto::id }

        // also works with nested
        map { SourceDto::nested / SourceDto.Nested::srcUuid } into { TargetDto::nested / TargetDto.Nested::uuid }

        // can map from parent to child
        map { SourceDto::toChild } into { TargetDto::nested / TargetDto.Nested::fromParent}

        // and vice versa
        map { SourceDto::nested / SourceDto.Nested::toParent } into { TargetDto::fromChild }
    }
})
```

For a full example please refer to [kotstruct-example](./kotstruct-example).
