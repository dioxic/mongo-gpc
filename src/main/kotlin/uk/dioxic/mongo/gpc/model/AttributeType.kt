package uk.dioxic.mongo.gpc.model

import com.github.jershell.kbson.NonEncodeNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AttributeType(
    @SerialName("_id")
    override val code: Int,
    override val text: String,
    @NonEncodeNull
    override val definition: String? = null,
    val values: List<AttributeValue>
) : Definition {

    class Builder : DefinitionBuilder() {
        private var attributeValues: MutableList<AttributeValue> = mutableListOf()

        fun attributeValue(attributeValue: AttributeValue) = apply { this.attributeValues.add(attributeValue) }

        fun build() = AttributeType(code!!, text!!, definition, attributeValues)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AttributeType

        if (code != other.code) return false
        if (text != other.text) return false
        if (definition != other.definition) return false

        return true
    }

    override fun hashCode(): Int {
        var result = code
        result = 31 * result + text.hashCode()
        result = 31 * result + (definition?.hashCode() ?: 0)
        return result
    }


}