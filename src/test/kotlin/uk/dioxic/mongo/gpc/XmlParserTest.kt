package uk.dioxic.mongo.gpc

import org.junit.jupiter.api.Test
import java.nio.file.Path

class XmlParserTest {

    private fun String.asResourcePath(): Path =
        Path.of(CsvTest::class.java.getResource(this).toURI())

    @Test
    fun test() {
        parseGpcXml("/example.xml".asResourcePath())
            .forEach {
                println(it)
            }
    }

}