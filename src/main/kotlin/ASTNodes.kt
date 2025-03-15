package org.example

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Node {
    val kind: String
}

@Serializable
data class ProgramNode(
    val functions: List<FunctionDeclarationNode>,
    override val kind: String = "program",
) : Node

@Serializable
data class FunctionDeclarationNode(
    val name: String,
    val arguments: List<ArgumentNode>,
    val returnType: String,
    val body: BlockNode,
    override val kind: String = "function_declaration"
) : Node

@Serializable
data class ArgumentNode(
    val name: String,
    @SerialName("type")
    val type: String,
    override val kind: String = "argument"
) : Node

@Serializable
data class BlockNode(
    val statements: List<Node>,
    override val kind: String = "block"
) : Node

@Serializable
data class VariableDeclarationNode(
    val name: String,
    @SerialName("type")
    val type: String,
    val value: Node?,
    override val kind: String = "variable_declaration"
) : Node

@Serializable
data class AssignmentNode(
    val name: String,
    val value: Node,
    override val kind: String = "assignment"
) : Node

@Serializable
data class IfNode(
    val predicate: Node,
    val body: BlockNode,
    val elseBlock: BlockNode? = null,
    override val kind: String = "if_statement"
) : Node

@Serializable
data class ReturnNode(
    val value: Node,
    override val kind: String = "return_statement"
) : Node

@Serializable
data class FunctionCallNode(
    val name: String,
    val arguments: List<Node>,
    override val kind: String = "function_call"
) : Node

@Serializable
data class BinaryOperatorNode(
    val operator: String,
    val left: Node,
    val right: Node,
    override val kind: String = "binary_operator"
) : Node

@Serializable
data class PrefixOperatorNode(
    val operator: String,
    val right: Node,
    override val kind: String = "prefix_operator"
) : Node

@Serializable
data class VariableNode(
    val name: String,
    override val kind: String = "variable"
) : Node

@Serializable
data class NumberLiteralNode(
    val value: Int,
    override val kind: String = "number_literal"
) : Node

@Serializable
data class StringLiteralNode(
    val string: String,
    override val kind: String = "string_literal"
) : Node