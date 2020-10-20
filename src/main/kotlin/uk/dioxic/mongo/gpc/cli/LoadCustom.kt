package uk.dioxic.mongo.gpc.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.cooccurring
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.mongodb.MongoClientSettings
import org.litote.kmongo.KMongo
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.save
import uk.dioxic.mongo.gpc.model.Brick

@ExperimentalStdlibApi
class LoadCustom : CliktCommand(name = "loadCustom") {

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

        println("locating GPC Spirits Brick")

        val brickCode = 10000263

        val session = client.startSession()

        val collection = client
            .getDatabase(namespaceOptions.database)
            .getCollection<Brick>("brick")

        session.withTransaction {
            val spiritBrick = collection
                .findOneById(session, brickCode)
                ?: throw RuntimeException("Spirits brick (code:$brickCode) not found!")

            println("marking GPC Sprits brick as inactive")
            collection
                .save(session, spiritBrick.copy(active = false))

            println("loading custom bricks data")

            val customBrickCommon = spiritBrick.copy(parentBrickCode = spiritBrick.code, source = "JS")

            val customBricks = listOf(
                customBrickCommon.copy(code = 90000001, text = "Whiskey", definition = "My custom Whiskey"),
                customBrickCommon.copy(code = 90000002, text = "Gin", definition = "My custom Gin"),
                customBrickCommon.copy(code = 90000003, text = "Rum", definition = "My custom Rum"),
                customBrickCommon.copy(code = 90000004, text = "Vodka", definition = "My custom Vodka"),
                customBrickCommon.copy(code = 90000005, text = "Brandy", definition = "My custom Brandy"),
                customBrickCommon.copy(code = 90000006, text = "Cognac", definition = "My custom Cognac"),
            )

            collection
                .insertMany(session, customBricks)
        }


        println("completed")
    }

}