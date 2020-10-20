package uk.dioxic.mongo.gpc.model

open class DefinitionBuilder {
    var code: Int? = null
        private set
    var text: String? = null
        private set
    var definition: String? = null
        private set

    fun code(code: Int) = apply { this.code = code }
    fun text(text: String) = apply { this.text = text }
    fun definition(definition: String) = apply { this.definition = definition }
}