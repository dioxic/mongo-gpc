package uk.dioxic.mongo.gpc

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import kotlinx.serialization.Serializable
import uk.dioxic.mongo.gpc.model.AttributeType
import uk.dioxic.mongo.gpc.model.AttributeValue
import uk.dioxic.mongo.gpc.model.Brick
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

@ExperimentalStdlibApi
fun processCsv(reader: InputStream, action: (List<Brick>) -> Unit) {
    csvReader {
        skipEmptyLine = true
    }.open(reader) {
        action(readAllAsSequence()
            .filterIndexed { index, _ -> index > 0 }
            .map { gpcCsvOf(it) }
            .groupBy { it.brickKeySelector() }
            .map { (brickKeySelector, brickRows) ->
                val attrTypes = brickRows
                    .groupBy { it.attributeTypeSelector() }
                    .mapNotNull { (attrKeySelector, brickRows) ->
                        if (attrKeySelector != null) {
                            attributeTypeOf(attrKeySelector, brickRows.mapNotNull { it.attributeValueSelector() })
                        } else {
                            null
                        }
                    }
                brickOf(brickKeySelector, attrTypes)
            })
    }
}

@ExperimentalStdlibApi
fun processCsvRaw(reader: InputStream, action: (GpcCsv) -> Unit) {
    csvReader {
        skipEmptyLine = true
    }.open(reader) {
        readAllAsSequence()
            .filterIndexed { index, _ -> index > 0 }
            .map { gpcCsvOf(it) }
            .forEach {
                action(it)
            }
    }
}

@ExperimentalStdlibApi
fun processCsv(path: Path, action: (List<Brick>) -> Unit) {
    processCsv(Files.newInputStream(path), action)
}

@ExperimentalStdlibApi
fun processCsvRaw(path: Path, action: (GpcCsv) -> Unit) {
    processCsvRaw(Files.newInputStream(path), action)
}

@Serializable
data class GpcCsv(
    val segmentCode: Int,
    val segmentDescription: String,
    val familyCode: Int,
    val familyDescription: String,
    val classCode: Int,
    val classDescription: String,
    val brickCode: Int,
    val brickDescription: String,
    val attributeTypeCode: Int? = null,
    val attributeTypeDescription: String? = null,
    val attributeValueCode: Int? = null,
    val attributeValueDescription: String? = null,
) {
    fun brickKeySelector(): BrickKeySelector =
        BrickKeySelector(
            segmentCode,
            segmentDescription,
            familyCode,
            familyDescription,
            classCode,
            classDescription,
            brickCode,
            brickDescription
        )

    fun attributeTypeSelector(): AttributeTypeSelector? =
        if (hasCoreAttribute()) {
            AttributeTypeSelector(
                code = attributeTypeCode!!,
                description = attributeTypeDescription!!
            )
        } else {
            null
        }

    fun attributeValueSelector(): AttributeValue? =
        if (hasCoreAttribute()) {
            AttributeValue(
                code = attributeValueCode!!,
                text = attributeValueDescription!!
            )
        } else {
            null
        }

    private fun hasCoreAttribute() = attributeTypeCode != null
            && attributeTypeDescription != null
            && attributeValueCode != null
            && attributeValueDescription != null

}

fun gpcCsvOf(fields: List<String>): GpcCsv {
    if (fields.size != 12) {
        throw IllegalStateException("unexpected fields count for: $fields")
    }

    try {
        return GpcCsv(
            segmentCode = fields[0].toInt(),
            segmentDescription = fields[1],
            familyCode = fields[2].toInt(),
            familyDescription = fields[3],
            classCode = fields[4].toInt(),
            classDescription = fields[5],
            brickCode = fields[6].toInt(),
            brickDescription = fields[7],
            attributeTypeCode = fields[8].toIntOrNull(),
            attributeTypeDescription = fields[9].ifBlank { null },
            attributeValueCode = fields[10].toIntOrNull(),
            attributeValueDescription = fields[11]
        )
    } catch (e: Exception) {
        println("error parsing: $fields")
        throw e
    }

}

fun attributeTypeOf(attributeTypeSelector: AttributeTypeSelector, values: List<AttributeValue>) =
    AttributeType(
        code = attributeTypeSelector.code,
        text = attributeTypeSelector.description,
        values = values
    )


fun brickOf(keySelector: BrickKeySelector, attributes: List<AttributeType>) =
    Brick(
        segmentCode = keySelector.segmentCode,
        familyCode = keySelector.familyCode,
        classCode = keySelector.classCode,
        code = keySelector.brickCode,
        text = keySelector.brickDescription,
        attributes = attributes
    )

data class BrickKeySelector(
    val segmentCode: Int,
    val segmentDescription: String,
    val familyCode: Int,
    val familyDescription: String,
    val classCode: Int,
    val classDescription: String,
    val brickCode: Int,
    val brickDescription: String,
)

data class AttributeTypeSelector(
    val code: Int,
    val description: String
)