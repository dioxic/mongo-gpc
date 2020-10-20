package uk.dioxic.mongo.gpc.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.groups.cooccurring
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import com.mongodb.MongoClientSettings
import org.litote.kmongo.KMongo
import org.litote.kmongo.deleteMany
import org.litote.kmongo.getCollection
import uk.dioxic.mongo.gpc.model.*
import uk.dioxic.mongo.gpc.parseGpcXml
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@ExperimentalStdlibApi
class LoadXml : CliktCommand(name = "loadXml") {

    private val xmlFile by argument(help = "xml file to import").path(mustExist = true, canBeDir = false)
    private val authOptions by AuthOptions().cooccurring()
    private val connOptions by ConnectionOptions()
    private val namespaceOptions by NamespaceOptions()
    private val drop by option(help = "drop collection before load").flag()
    private val delete by option(help = "delete docs from collection before load").flag()

    @ExperimentalTime
    override fun run() {
        val client = KMongo.createClient(
            MongoClientSettings.builder()
                .applyAuthOptions(authOptions)
                .applyConnectionOptions(connOptions)
                .build()
        )

        val database = client
            .getDatabase(namespaceOptions.database)

        if (drop) {
            println("dropping database ${namespaceOptions.database}")
            database.drop()
        }

        if (delete) {
            println("deleting documents from all collections")
            database.getCollection("segment").deleteMany()
            database.getCollection("family").deleteMany()
            database.getCollection("class").deleteMany()
            database.getCollection("brick").deleteMany()
            database.getCollection("attType").deleteMany()
            database.getCollection("attValue").deleteMany()
        }

        val duration = measureTime {

            println("parsing XML")
            val fullModel = parseGpcXml(xmlFile)

            println("loading segments")
            database.getCollection<Segment>("segment")
                .insertMany(fullModel.filterIsInstance<Segment>())

            println("loading families")
            database.getCollection<Family>("family")
                .insertMany(fullModel.filterIsInstance<Family>())

            println("loading classes")
            database.getCollection<Class>("class")
                .insertMany(fullModel.filterIsInstance<Class>())

            println("loading bricks")
            database.getCollection<Brick>("brick")
                .insertMany(fullModel.filterIsInstance<Brick>())

            println("loading attribute types")
            database.getCollection<AttributeType>("attType")
                .insertMany(fullModel.filterIsInstance<AttributeType>())

            println("loading attribute values")
            database.getCollection<AttributeValue>("attValue")
                .insertMany(fullModel.filterIsInstance<AttributeValue>())

        }

        println("completed in $duration")
    }

}