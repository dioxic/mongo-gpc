package uk.dioxic.mongo.gpc.repository

import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.ReplaceOptions
import org.litote.kmongo.getCollection
import org.litote.kmongo.replaceOneById
import uk.dioxic.mongo.gpc.model.*

@Deprecated(message = "no longer used")
class MongoRepository(private val database: MongoDatabase) : ModelRepository {

    private val replaceOptions = ReplaceOptions().upsert(true)

    override suspend fun save(segment: Segment) {
        database.getCollection<Segment>("segment")
            .replaceOneById(segment.code, segment, replaceOptions)
    }

    override suspend fun save(family: Family) {
        database.getCollection<Family>("family")
            .replaceOneById(family.code, family, replaceOptions)
    }

    override suspend fun save(clazz: Class) {
        database.getCollection<Class>("class")
            .replaceOneById(clazz.code, clazz, replaceOptions)
    }

    override suspend fun save(brick: Brick) {
        database.getCollection<Brick>("brick")
            .replaceOneById(brick.code, brick, replaceOptions)
    }

    override suspend fun save(attributeType: AttributeType) {
        database.getCollection<AttributeType>("attType")
            .replaceOneById(attributeType.code, attributeType, replaceOptions)
    }

    override suspend fun save(attributeValue: AttributeValue) {
        database.getCollection<AttributeValue>("attValue")
            .replaceOneById(attributeValue.code, attributeValue, replaceOptions)
    }

}