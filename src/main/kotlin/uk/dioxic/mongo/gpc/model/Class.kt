package uk.dioxic.mongo.gpc.model

import com.github.jershell.kbson.NonEncodeNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Class(
    @SerialName("_id")
    override val code: Int,
    override val text: String,
    @NonEncodeNull
    override val definition: String? = null,
    val segmentCode: Int,
    val familyCode: Int
) : Definition {

    class Builder : DefinitionBuilder() {
        private var segmentCode: Int? = null
        private var familyCode: Int? = null

        fun segmentCode(segmentCode: Int) = apply { this.segmentCode = segmentCode }
        fun familyCode(familyCode: Int) = apply { this.familyCode = familyCode }
        fun build() = Class(code!!, text!!, definition, segmentCode!!, familyCode!!)
    }
}