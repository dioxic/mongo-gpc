package uk.dioxic.mongo.gpc

import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

class CsvTest {

    private fun String.asResourcePath(): Path =
        Path.of(CsvTest::class.java.getResource(this).toURI())

    @Test
    @ExperimentalStdlibApi
    fun readExample() {
        processCsv(Files.newInputStream("/example.csv".asResourcePath())) {
            println(it)
        }
    }

}