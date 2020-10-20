package uk.dioxic.mongo.gpc.repository

import uk.dioxic.mongo.gpc.model.*

@Deprecated(message = "no longer used")
class PrintRepository: ModelRepository {

    override suspend fun save(segment: Segment) {
        println(segment)
    }

    override suspend fun save(family: Family) {
        println(family)
    }

    override suspend fun save(clazz: Class) {
        println(clazz)
    }

    override suspend fun save(brick: Brick) {
        println(brick)
    }

    override suspend fun save(attributeType: AttributeType) {
        println(attributeType)
    }

    override suspend fun save(attributeValue: AttributeValue) {
        println(attributeValue)
    }

}