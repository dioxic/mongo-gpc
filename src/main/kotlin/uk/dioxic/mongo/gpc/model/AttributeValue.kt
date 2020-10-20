package uk.dioxic.mongo.gpc.model

import com.github.jershell.kbson.NonEncodeNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AttributeValue(
    @SerialName("_id")
    override val code: Int,
    override val text: String,
    @NonEncodeNull
    override val definition: String? = null
) : Definition {
    class Builder : DefinitionBuilder() {
        fun build() = AttributeValue(code!!, text!!, definition)
    }
}