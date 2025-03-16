package org.example

class Parser(
    tokens: Sequence<Token>,
    private val logger: ErrorLogger
) {
    private val iterator = object {
        private var tokenIterator = tokens.iterator()
        private var currentToken: Token = tokenIterator.next()
        private var lookaheadToken: Token? = null
        fun peek(): Token = currentToken
        fun next(): Token? {
            if (lookaheadToken != null) {
                currentToken = lookaheadToken!!
                lookaheadToken = null
                return currentToken
            }
            if (!tokenIterator.hasNext()) return null
            currentToken = tokenIterator.next()
            return currentToken
        }

        fun peekNext(): Token? {
            if (lookaheadToken != null) return lookaheadToken
            if (!tokenIterator.hasNext()) return null
            lookaheadToken = tokenIterator.next()
            return lookaheadToken
        }

        fun expect(type: TokenType): Token {
            val token = peek()
            if (token.type == type) {
                val nextToken = next()
                if (nextToken != null)
                    currentToken = nextToken
                return token
            } else {
                logger.logError(
                    currentToken.position, currentToken.line, currentToken.column,
                    "Expected token $type but found ${token.type.name}"
                )
                throw IllegalArgumentException()
            }
        }
    }

    private fun peek(): Token = iterator.peek()
    private fun next(): Token? = iterator.next()
    private fun peekNext(): Token? = iterator.peekNext()
    private fun expect(type: TokenType): Token = iterator.expect(type)

    private fun reportUnexpectedToken() {
        val token = peek()
        logger.logError(
            token.position, token.line, token.column,
            "Unexpected token of type ${token.type.name}"
        )
    }

    private fun parseProgram(): ProgramNode {
        val functions = mutableListOf<FunctionDeclarationNode>()
        while (peek().type == TokenType.FUN) {
            functions.add(parseFunctionDeclaration())
        }
        expect(TokenType.EOF)
        return ProgramNode(functions)
    }

    private fun parseFunctionDeclaration(): FunctionDeclarationNode {
        expect(TokenType.FUN)
        val identifier = expect(TokenType.IDENTIFIER)
        expect(TokenType.PAREN_OPEN)
        val arguments = if (peek().type != TokenType.PAREN_CLOSE) parseArgumentList() else emptyList()
        expect(TokenType.PAREN_CLOSE)
        val returnType = if (peek().type == TokenType.COLON) {
            expect(TokenType.COLON)
            expect(TokenType.IDENTIFIER)
        } else null
        val block = parseBlock()
        return FunctionDeclarationNode(
            identifier.value,
            arguments,
            returnType?.value ?: "void",
            block
        )
    }

    private fun parseArgumentList(): List<ArgumentNode> {
        val arguments = mutableListOf<ArgumentNode>()
        arguments.add(parseArgument())
        while (peek().type == TokenType.COMMA) {
            expect(TokenType.COMMA)
            arguments.add(parseArgument())
        }
        return arguments
    }

    private fun parseArgument(): ArgumentNode {
        val identifier = expect(TokenType.IDENTIFIER)
        expect(TokenType.COLON)
        val type = expect(TokenType.IDENTIFIER)
        return ArgumentNode(identifier.value, type.value)
    }

    private fun parseBlock(): BlockNode {
        expect(TokenType.BRACE_OPEN)
        val statements = mutableListOf<Node>()
        while (peek().type != TokenType.BRACE_CLOSE) {
            statements.add(parseStatement())
        }
        expect(TokenType.BRACE_CLOSE)
        return BlockNode(statements)
    }

    private fun parseStatement(): Node {
        return when (peek().type) {
            TokenType.IDENTIFIER -> {
                val nextToken = peekNext()
                when (nextToken?.type) {
                    TokenType.PAREN_OPEN -> {
                        parseFunctionCall().also { expect(TokenType.SEMICOLON) }
                    }

                    TokenType.IDENTIFIER -> parseVariableDeclaration()
                    else -> if (nextToken?.value == "=") parseAssignment() else {
                        parseExpression().also { expect(TokenType.SEMICOLON) }
                    }
                }
            }

            TokenType.IF -> parseIfStatement()
            TokenType.RETURN -> parseReturnStatement()
            else -> {
                reportUnexpectedToken()
                throw IllegalArgumentException()
            }
        }
    }

    private fun parseVariableDeclaration(): VariableDeclarationNode {
        val type = expect(TokenType.IDENTIFIER)
        val identifier = expect(TokenType.IDENTIFIER)
        val expression = if (peek().type == TokenType.OPERATOR && peek().value == "=") {
            expect(TokenType.OPERATOR)
            parseExpression()
        } else null
        expect(TokenType.SEMICOLON)
        return VariableDeclarationNode(identifier.value, type.value, expression)
    }

    private fun parseAssignment(): AssignmentNode {
        val identifier = expect(TokenType.IDENTIFIER)
        expect(TokenType.OPERATOR)
        val expression = parseExpression()
        expect(TokenType.SEMICOLON)
        return AssignmentNode(identifier.value, expression)
    }

    private fun parseFunctionCall(): FunctionCallNode {
        val identifier = expect(TokenType.IDENTIFIER)
        expect(TokenType.PAREN_OPEN)
        val arguments = if (peek().type != TokenType.PAREN_CLOSE) parseExpressionList() else emptyList()
        expect(TokenType.PAREN_CLOSE)
        return FunctionCallNode(identifier.value, arguments)
    }

    private fun parseIfStatement(): IfNode {
        expect(TokenType.IF)
        expect(TokenType.PAREN_OPEN)
        val predicate = parseExpression()
        expect(TokenType.PAREN_CLOSE)
        val thenBlock = parseBlock()
        val elseBlock = if (peek().type == TokenType.ELSE) {
            expect(TokenType.ELSE)
            parseBlock()
        } else null
        return IfNode(predicate, thenBlock, elseBlock)
    }

    private fun parseReturnStatement(): ReturnNode {
        expect(TokenType.RETURN)
        val expression = parseExpression()
        expect(TokenType.SEMICOLON)
        return ReturnNode(expression)
    }

    private fun getOperatorPrecedence(token: Token): Int {
        return when (token.value) {
            "*", "/", "%" -> 30
            "+", "-" -> 20
            ">", "<", ">=", "<=", "==", "!=" -> 10
            else -> 0
        }
    }

    private fun parseExpression(): Node {
        return parseBinaryOperator(0)
    }

    private fun parseBinaryOperator(precedence: Int): Node {
        var left: Node = parseTerm()
        while (true) {
            val operator = peek()
            if (operator.type != TokenType.OPERATOR) break
            val operatorPrecedence = getOperatorPrecedence(operator)
            if (operatorPrecedence < precedence) {
                return left
            }
            next()
            val right = parseBinaryOperator(operatorPrecedence + 1)

            left = BinaryOperatorNode(operator.value, left, right)
        }
        return left
    }

    private fun Token.isPrefixOperator(): Boolean {
        return type == TokenType.OPERATOR && value == "-"
    }

    private fun parseTerm(): Node {
        if (peek().isPrefixOperator()) {
            val operator = expect(TokenType.OPERATOR)
            return PrefixOperatorNode(operator.value, parseTerm())
        }
        return when (peek().type) {
            TokenType.IDENTIFIER -> {
                val nextToken = peekNext()
                when (nextToken?.type) {
                    TokenType.PAREN_OPEN -> parseFunctionCall()
                    else -> VariableNode(expect(TokenType.IDENTIFIER).value)
                }

            }

            TokenType.NUMBER -> NumberLiteralNode(expect(TokenType.NUMBER).value.toInt())
            TokenType.STRING -> StringLiteralNode(expect(TokenType.STRING).value)
            TokenType.PAREN_OPEN -> {
                expect(TokenType.PAREN_OPEN)
                val expression = parseExpression()
                expect(TokenType.PAREN_CLOSE)
                expression
            }

            else -> {
                reportUnexpectedToken()
                throw IllegalArgumentException()
            }
        }
    }

    private fun parseExpressionList(): List<Node> {
        val expressions = mutableListOf<Node>()
        expressions.add(parseExpression())
        while (peek().type == TokenType.COMMA) {
            expect(TokenType.COMMA)
            expressions.add(parseExpression())
        }
        return expressions
    }

    fun parse(): Node {
        return parseProgram()
    }
}
