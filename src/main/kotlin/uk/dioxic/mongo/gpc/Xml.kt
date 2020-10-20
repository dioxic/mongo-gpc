package uk.dioxic.mongo.gpc

import uk.dioxic.mongo.gpc.model.*
import java.nio.file.Path
import javax.xml.stream.XMLEventReader
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants
import javax.xml.stream.events.Attribute
import javax.xml.stream.events.XMLEvent

fun parseGpcXml(path: Path): Set<Definition> {
    val factory: XMLInputFactory = XMLInputFactory.newInstance()
    val eventReader: XMLEventReader = factory.createXMLEventReader(path.toFile().reader())
    val entities: MutableSet<Definition> = mutableSetOf()

    lateinit var segmentBuilder: Segment.Builder
    lateinit var familyBuilder: Family.Builder
    lateinit var classBuilder: Class.Builder
    lateinit var brickBuilder: Brick.Builder
    lateinit var attTypeBuilder: AttributeType.Builder
    lateinit var attValueBuilder: AttributeValue.Builder

    while (eventReader.hasNext()) {
        val event: XMLEvent = eventReader.nextEvent()

        when (event.eventType) {
            XMLStreamConstants.START_ELEMENT -> {
                val startElement = event.asStartElement()

                when (startElement.name.localPart) {
                    "segment" -> {
                        segmentBuilder = Segment.Builder()
                        segmentBuilder.apply(startElement.attributes)
                    }
                    "family" -> {
                        familyBuilder = Family.Builder()
                        familyBuilder.apply(startElement.attributes)
                    }
                    "class" -> {
                        classBuilder = Class.Builder()
                        classBuilder.apply(startElement.attributes)
                    }
                    "brick" -> {
                        brickBuilder = Brick.Builder()
                        brickBuilder.apply(startElement.attributes)
                    }
                    "attType" -> {
                        attTypeBuilder = AttributeType.Builder()
                        attTypeBuilder.apply(startElement.attributes)
                    }
                    "attValue" -> {
                        attValueBuilder = AttributeValue.Builder()
                        attValueBuilder.apply(startElement.attributes)
                    }
                }
            }
            XMLStreamConstants.END_ELEMENT -> {
                val endElement = event.asEndElement()
                when (endElement.name.localPart) {
                    "segment" -> {
                        entities.add(segmentBuilder.build())
                    }
                    "family" -> {
                        entities.add(
                            familyBuilder
                                .segmentCode(segmentBuilder.code!!)
                                .build()
                        )
                    }
                    "class" -> {
                        entities.add(
                            classBuilder
                                .segmentCode(segmentBuilder.code!!)
                                .familyCode(familyBuilder.code!!)
                                .build()
                        )
                    }
                    "brick" -> {
                        entities.add(
                            brickBuilder
                                .segmentCode(segmentBuilder.code!!)
                                .familyCode(familyBuilder.code!!)
                                .classCode(classBuilder.code!!)
                                .build()
                        )
                    }
                    "attType" -> {
                        with(attTypeBuilder.build()) {
                            // we don't want the definition field in the embedded types so
                            // make a copy and set definition to null
                            brickBuilder.attribute(
                                this.copy(
                                    definition = null,
                                    values = this.values.map { it.copy(definition = null) })
                            )
                            entities.add(this)
                        }
                    }
                    "attValue" -> {
                        with(attValueBuilder.build()) {
                            attTypeBuilder.attributeValue(this.copy(definition = null))
                            entities.add(this)
                        }
                    }
                }
            }
        }
    }
    return entities
}

fun DefinitionBuilder.apply(attributes: Iterator<Attribute>) = apply {
    attributes.forEach {
        when (it.name.localPart) {
            "code" -> code(it.value.toInt())
            "text" -> text(it.value)
            "definition" -> if (it.value.isNotBlank()) definition(it.value)
        }
    }
}