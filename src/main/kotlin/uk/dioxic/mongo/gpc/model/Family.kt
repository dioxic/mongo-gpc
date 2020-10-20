package uk.dioxic.mongo.gpc.model

import com.github.jershell.kbson.NonEncodeNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Family(
    @SerialName("_id")
    override val code: Int,
    override val text: String,
    @NonEncodeNull
    override val definition: String? = null,
    val segmentCode: Int,
) : Definition {

    class Builder : DefinitionBuilder() {
        private var segmentCode: Int? = null

        fun segmentCode(segmentCode: Int) = apply { this.segmentCode = segmentCode }
        fun build() = Family(code!!, text!!, definition, segmentCode!!)
    }
}