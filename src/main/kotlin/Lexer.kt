package org.example


class Lexer(private val input: String, private val logger: ErrorLogger) {
    private var start = 0
    private var startColumn = 0
    private var position = 0
    private var line = 1
    private var column = 1

    private fun Char.isLetter(): Boolean = this in 'a'..'z' || this in 'A'..'Z' || this == '_'

    private fun Char.isLetterOrDigit(): Boolean = this.isLetter() || this.isDigit()

    private fun Char.isDigit(): Boolean = this in '0'..'9'

    fun tokenize(): List<Token> {
        val tokens = mutableListOf<Token>()
        while (!endOfFile()) {
            start = position
            startColumn = column
            scanToken()?.let { tokens.add(it) }
        }

        tokens.add(Token(TokenType.EOF, "", position, line, column))
        return tokens
    }

    private fun scanToken(): Token? {
        val c = next()
        return if (c.isDigit()) {
            parseNumberLiteral()
        } else if (c.isLetter()) {
            parseIdentifier()
        } else when (c) {
            '(' -> Token(TokenType.PAREN_OPEN, "(", start, line, startColumn)
            ')' -> Token(TokenType.PAREN_CLOSE, ")", start, line, startColumn)
            '{' -> Token(TokenType.BRACE_OPEN, "{", start, line, startColumn)
            '}' -> Token(TokenType.BRACE_CLOSE, "}", start, line, startColumn)
            ',' -> Token(TokenType.COMMA, ",", start, line, startColumn)
            '-' -> Token(TokenType.OPERATOR, "-", start, line, startColumn)
            '+' -> Token(TokenType.OPERATOR, "+", start, line, startColumn)
            ':' -> Token(TokenType.COLON, ":", start, line, startColumn)
            ';' -> Token(TokenType.SEMICOLON, ";", start, line, startColumn)
            '*' -> Token(TokenType.OPERATOR, "*", start, line, startColumn)
            '%' -> Token(TokenType.OPERATOR, "%", start, line, startColumn)
            '"' -> parseStringLiteral()
            '!' -> if (match('=')) Token(TokenType.OPERATOR, "!=", start, line, startColumn)
            else {
                logger.logError(start, line, startColumn, "Unexpected character")
                throw IllegalArgumentException()
            }

            '=' -> if (match('=')) Token(TokenType.OPERATOR, "==", start, line, startColumn)
            else Token(TokenType.OPERATOR, "=", start, line, startColumn)

            '<' -> if (match('=')) Token(TokenType.OPERATOR, "<=", start, line, startColumn)
            else Token(TokenType.OPERATOR, "<", start, line, startColumn)

            '>' -> if (match('=')) Token(TokenType.OPERATOR, ">=", start, line, startColumn)
            else Token(TokenType.OPERATOR, ">", start, line, startColumn)

            '/' -> {
                if (match('/')) {
                    while (peek() != '\n' && !endOfFile()) next()
                    Token(TokenType.COMMENT, input.substring(start, position), start, line, startColumn)
                } else {
                    Token(TokenType.OPERATOR, "/", start, line, startColumn)
                }
            }

            ' ', '\r', '\t' -> {
                null
            }

            '\n' -> {
                newLine()
                null
            }

            else -> {
                logger.logError(start, line, startColumn, "Unexpected character")
                throw IllegalArgumentException()
            }
        }
    }

    private fun parseIdentifier(): Token {
        while (peek()?.isLetterOrDigit() == true) next()

        return when (val text: String = input.substring(start, position)) {
            "fun" -> Token(TokenType.FUN, text, start, line, startColumn)
            "if" -> Token(TokenType.IF, text, start, line, startColumn)
            "else" -> Token(TokenType.ELSE, text, start, line, startColumn)
            "return" -> Token(TokenType.RETURN, text, start, line, startColumn)
            else -> Token(TokenType.IDENTIFIER, text, start, line, startColumn)
        }
    }

    private fun parseNumberLiteral(): Token {
        while (peek()?.isDigit() == true) next()

        return Token(TokenType.NUMBER, input.substring(start, position), start, line, startColumn)
    }

    private fun parseStringLiteral(): Token {
        while (peek() != '"' && peek() != '\n' && !endOfFile()) {
            next()
        }

        if (peek() != '"') {
            logger.logError(start, line, startColumn, "Unterminated string literal")
            throw IllegalArgumentException()
        }
        next()

        val value: String = input.substring(start + 1, position - 1)
        return Token(TokenType.STRING, value, start, line, startColumn)
    }

    private fun match(expected: Char): Boolean {
        if (input.getOrNull(position) != expected) return false
        ++position
        return true
    }

    private fun peek(): Char? = input.getOrNull(position)

    private fun endOfFile(): Boolean = position >= input.length

    private fun next(): Char {
        ++column
        return input[position++]
    }

    private fun newLine() {
        ++line
        column = 1
    }
}
