package uk.dioxic.mongo.gpc.repository

import uk.dioxic.mongo.gpc.model.*

@Deprecated(message = "no longer used")
interface ModelRepository {

    suspend fun save(definition: Definition) {
        when(definition) {
            is Segment -> save(definition)
            is Family -> save(definition)
            is Class -> save(definition)
            is Brick -> save(definition)
            is AttributeType -> save(definition)
            is AttributeValue -> save(definition)
            else -> throw IllegalArgumentException("definition ${definition::class} cannot be saved!")
        }
    }

    suspend fun save(segment: Segment)
    suspend fun save(family: Family)
    suspend fun save(clazz: Class)
    suspend fun save(brick: Brick)
    suspend fun save(attributeType: AttributeType)
    suspend fun save(attributeValue: AttributeValue)

}