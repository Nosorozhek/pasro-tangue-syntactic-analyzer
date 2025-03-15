package org.example

import kotlinx.serialization.Serializable
import java.util.regex.Pattern

enum class TokenType(pattern: String, val ignore: Boolean = false) {
    FUN("fun"),
    IF("if"),
    ELSE("else"),
    RETURN("return"),

    NUMBER("\\d+"),
    STRING("\"[^\"\n]*\""),
    IDENTIFIER("[a-zA-Z_][a-zA-Z0-9_]*"),
    OPERATOR("-|==|!=|<=|>=|<|>|[+]|=|[*]|/|%"),
    COMMA(","),
    COLON(":"),
    SEMICOLON(";"),
    PAREN_OPEN("\\("),
    PAREN_CLOSE("\\)"),
    BRACE_OPEN("\\{"),
    BRACE_CLOSE("\\}"),
    WHITESPACE("[\\s^\n]+", true);


    val regex: Pattern = Pattern.compile(pattern)
}

@Serializable
data class Token(val type: TokenType, val value: String, val position: Int, val line: Int, val column: Int)


class Lexer(private val input: String) {
    private var position = 0
    private var line = 1
    private var column = 1

    fun tokenize(): List<Token> {
        val tokens = mutableListOf<Token>()

        while (position < input.length) {
            var matched = false

            for (tokenType in TokenType.entries) {
                val matcher = tokenType.regex.matcher(input.substring(position))
                if (matcher.lookingAt()) {
                    val match = matcher.group()
                    if (!tokenType.ignore) {
                        tokens.add(Token(tokenType, match, position, line, column))
                    }
                    advance(match)
                    matched = true
                    break
                }
            }

            if (!matched) {
                throw IllegalArgumentException("Unexpected character at $line:$column \"${input[position]}\"")
            }
        }

        return tokens
    }

    private fun advance(text: String) {
        for (char in text) {
            if (char == '\n') {
                line++
                column = 1
            } else {
                column++
            }
            position++
        }
    }
}
