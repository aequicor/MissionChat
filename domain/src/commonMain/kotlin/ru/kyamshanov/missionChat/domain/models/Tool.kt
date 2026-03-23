package ru.kyamshanov.missionChat.domain.models


/**
 * Enumeration of specific tool implementations.
 */
enum class ToolEnum : Tool {


}

/**
 * Represents a tool that can be used within the mission chat domain.
 */
internal interface Tool {

    /**
     * The static type of the tool.
     */
    val type: StaticToolType

    /**
     * The function declaration if the tool type is [StaticToolType.FUNCTION].
     */
    val function: FunctionDeclaration?
}


/**
 * Enumeration of static tool types available for the assistant.
 */
internal enum class StaticToolType {

    /**
     * A tool that allows the assistant to call custom functions defined in the API.
     */
    FUNCTION,

    /**
     * A tool that enables the assistant to write and execute Python code in a sandboxed environment.
     */
    CODE_INTERPRETER,

    /**
     * A tool that allows the assistant to search through uploaded files to provide context-aware answers.
     */
    FILE_SEARCH,
}

/**
 * Declaration of a function that the assistant can call.
 *
 * @property name The name of the function to be called.
 * @property description A description of what the function does.
 * @property parameters The list of parameters the function accepts.
 * @property strict Whether to enable strict schema adherence for the function call.
 */
internal data class FunctionDeclaration(
    val name: String,
    val description: String? = null,
    val parameters: List<ToolParameter> = emptyList(),
    val strict: Boolean? = false
)

/**
 * Represents a parameter for a [FunctionDeclaration].
 *
 * @property name The name of the parameter.
 * @property description A description of what the parameter represents.
 * @property type The data type of the parameter (e.g., "string", "number").
 * @property availableValues An optional list of allowed values for this parameter.
 * @property isRequired Whether this parameter must be provided in the function call.
 */
internal data class ToolParameter(
    val name: String,
    val description: String,
    val type: String,
    val availableValues: List<Any>? = null,
    val isRequired: Boolean = true
)