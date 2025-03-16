package org.example

import kotlinx.serialization.Serializable

enum class TokenType {
    FUN,
    IF,
    ELSE,
    RETURN,

    NUMBER,
    STRING,
    IDENTIFIER,
    OPERATOR,

    COMMA,
    COLON,
    SEMICOLON,
    PAREN_OPEN,
    PAREN_CLOSE,
    BRACE_OPEN,
    BRACE_CLOSE,

    EOF;
}

@Serializable
data class Token(val type: TokenType, val value: String, val position: Int, val line: Int, val column: Int)
