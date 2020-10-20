package uk.dioxic.mongo.gpc.model

import com.github.jershell.kbson.NonEncodeNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Brick(
    @SerialName("_id")
    override val code: Int,
    override val text: String,
    @NonEncodeNull
    override val definition: String? = null,
    val segmentCode: Int,
    val familyCode: Int,
    val classCode: Int,
    @NonEncodeNull
    val parentBrickCode: Int? = null,
    val active: Boolean = true,
    val source: String = "GPC",
    val attributes: List<AttributeType>
) : Definition {

    class Builder : DefinitionBuilder() {
        private var segmentCode: Int? = null
        private var familyCode: Int? = null
        private var classCode: Int? = null
        private var parentBrickCode: Int? = null
        private var active: Boolean = true
        private var source: String = "GPC"
        private var attributeTypes: MutableList<AttributeType> = mutableListOf()

        fun segmentCode(segmentCode: Int) = apply { this.segmentCode = segmentCode }
        fun familyCode(familyCode: Int) = apply { this.familyCode = familyCode }
        fun classCode(classCode: Int) = apply { this.classCode = classCode }
        fun parentBrick(brickCode: Int) = apply { this.parentBrickCode = brickCode }
        fun active(active: Boolean) = apply { this.active = active }
        fun source(source: String) = apply { this.source = source }
        fun attribute(attributeType: AttributeType) = apply { this.attributeTypes.add(attributeType) }

        fun build() = Brick(
            code = code!!,
            text = text!!,
            definition = definition,
            segmentCode = segmentCode!!,
            familyCode = familyCode!!,
            classCode = classCode!!,
            parentBrickCode = parentBrickCode,
            active = active,
            source = source,
            attributes = attributeTypes
        )
    }

}