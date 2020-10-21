package uk.dioxic.mongo.gpc.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.cooccurring
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.mongodb.MongoClientSettings
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes.ascending
import org.bson.BsonDocument
import org.bson.BsonInt32
import org.bson.BsonString
import org.bson.conversions.Bson
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection
import uk.dioxic.mongo.gpc.model.Brick

@ExperimentalStdlibApi
class CreateIndexes : CliktCommand(name = "createIndexes", help = "creates indexes on the bricks collection") {

    private val authOptions by AuthOptions().cooccurring()
    private val connOptions by ConnectionOptions()
    private val namespaceOptions by NamespaceOptions()

    override fun run() {
        val client = KMongo.createClient(
            MongoClientSettings.builder()
                .applyAuthOptions(authOptions)
                .applyConnectionOptions(connOptions)
                .build()
        )

        val collection = client
            .getDatabase(namespaceOptions.database)
            .getCollection<Brick>(namespaceOptions.collection)

        println("creating indexes")
        with(collection) {
            createIndex(ascending("familyCode"))
            createIndex(ascending("classCode", "attribute.code"))
            createIndex(ascending("segmentCode"))
            createIndex(ascending("attribute.code"))

            val key: Bson = BsonDocument().apply {
                putAll(mapOf(
                    "text" to BsonString("text"),
                    "attributes.text" to BsonString("text"),
                    "attributes.values.text" to BsonString("text"),
                ))
            }

            val weights: Bson = BsonDocument().apply {
                putAll(mapOf(
                    "text" to BsonInt32(10),
                    "attributes.text" to BsonInt32(5),
                    "attributes.values.text" to BsonInt32(1),
                ))
            }

            createIndex(key, IndexOptions().name("textIdx").weights(weights))
        }

        println("completed")
    }

}