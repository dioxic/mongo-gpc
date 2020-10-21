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
import org.litote.kmongo.getCollection
import uk.dioxic.mongo.gpc.model.Brick
import uk.dioxic.mongo.gpc.processCsv

@ExperimentalStdlibApi
class LoadCsv : CliktCommand(name = "loadCsv", help = "Load GPC Bricks from CSV (defunct)") {

    private val csvFile by argument(help = "csv file to import").path(mustExist = true, canBeDir = false)
    private val authOptions by AuthOptions().cooccurring()
    private val connOptions by ConnectionOptions()
    private val namespaceOptions by NamespaceOptions()
    private val drop by option(help = "drop collection before load").flag()

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

        if (drop) {
            collection.drop()
        }

        println("loading data")
        processCsv(csvFile) {
            it.chunked(1000)
                .forEach { batch ->
                    collection.insertMany(batch)
                }
        }
        println("completed")
    }

}