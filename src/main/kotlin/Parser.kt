package org.example

class Parser(private val tokens: List<Token>) {
    private var position = 0

    private fun currentToken(): Token? = if (position < tokens.size) tokens[position] else null

    private fun expect(type: TokenType): Token {
        val token = currentToken()
        if (token?.type == type) {
            position++
            return token
        } else {
            throw IllegalArgumentException("Expected token $type but found ${token?.type} \"${token?.value}\" at position ${token?.position} (line ${token?.line}, column ${token?.column})")
        }
    }

    private fun parseProgram(): ProgramNode {
        val functions = mutableListOf<FunctionDeclarationNode>()
        while (currentToken()?.type == TokenType.FUN) {
            functions.add(parseFunctionDeclaration())
        }
        return ProgramNode(functions)
    }

    private fun parseFunctionDeclaration(): FunctionDeclarationNode {
        expect(TokenType.FUN)
        val identifier = expect(TokenType.IDENTIFIER)
        expect(TokenType.PAREN_OPEN)
        val arguments = if (currentToken()?.type != TokenType.PAREN_CLOSE) parseArgumentList() else emptyList()
        expect(TokenType.PAREN_CLOSE)
        val returnType = if (currentToken()?.type == TokenType.COLON) {
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
        while (currentToken()?.type == TokenType.COMMA) {
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
        while (currentToken()?.type != TokenType.BRACE_CLOSE) {
            statements.add(parseStatement())
        }
        expect(TokenType.BRACE_CLOSE)
        return BlockNode(statements)
    }

    private fun parseStatement(): Node {
        return when (currentToken()?.type) {
            TokenType.IDENTIFIER -> {
                val nextToken = tokens.getOrNull(position + 1)
                when (nextToken?.type) {
                    TokenType.PAREN_OPEN -> parseFunctionCall()
                    TokenType.IDENTIFIER -> parseVariableDeclaration()
                    else -> parseAssignment()
                }
            }

            TokenType.IF -> parseIfStatement()
            TokenType.RETURN -> parseReturnStatement()
            else -> throw IllegalArgumentException("Unexpected token ${currentToken()?.type} \"${currentToken()?.value}\" at position $position")
        }
    }

    private fun parseVariableDeclaration(): VariableDeclarationNode {
        val type = expect(TokenType.IDENTIFIER)
        val identifier = expect(TokenType.IDENTIFIER)
        val expression = if (currentToken()?.type == TokenType.OPERATOR && currentToken()?.value == "=") {
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
        val arguments = if (currentToken()?.type != TokenType.PAREN_CLOSE) parseExpressionList() else emptyList()
        expect(TokenType.PAREN_CLOSE)
        expect(TokenType.SEMICOLON)
        return FunctionCallNode(identifier.value, arguments)
    }

    private fun parseIfStatement(): IfNode {
        expect(TokenType.IF)
        expect(TokenType.PAREN_OPEN)
        val predicate = parseExpression()
        expect(TokenType.PAREN_CLOSE)
        val thenBlock = parseBlock()
        val elseBlock = if (currentToken()?.type == TokenType.ELSE) {
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
            val operator = currentToken() ?: break
            if (operator.type != TokenType.OPERATOR) break
            val operatorPrecedence = getOperatorPrecedence(operator)
            if (operatorPrecedence < precedence) {
                return left
            }
            position++
            val right = parseBinaryOperator(operatorPrecedence + 1)

            left = BinaryOperatorNode(operator.value, left, right)
        }
        return left
    }

    private fun Token.isPrefixOperator(): Boolean {
        return type == TokenType.OPERATOR && value == "-"
    }

    private fun parseTerm(): Node {
        if (currentToken()?.isPrefixOperator() == true) {
            val operator = expect(TokenType.OPERATOR)
            return PrefixOperatorNode(operator.value, parseTerm())
        }
        return when (currentToken()?.type) {
            TokenType.IDENTIFIER -> VariableNode(expect(TokenType.IDENTIFIER).value)
            TokenType.NUMBER -> NumberLiteralNode(expect(TokenType.NUMBER).value.toInt())
            TokenType.STRING -> StringLiteralNode(expect(TokenType.STRING).value)
            TokenType.PAREN_OPEN -> {
                expect(TokenType.PAREN_OPEN)
                val expression = parseExpression()
                expect(TokenType.PAREN_CLOSE)
                expression
            }

            else -> throw IllegalArgumentException("Unexpected token ${currentToken()?.type} \"${currentToken()?.value}\" at position $position")
        }
    }

    private fun parseExpressionList(): List<Node> {
        val expressions = mutableListOf<Node>()
        expressions.add(parseExpression())
        while (currentToken()?.type == TokenType.COMMA) {
            expect(TokenType.COMMA)
            expressions.add(parseExpression())
        }
        return expressions
    }

    fun parse(): Node {
        return parseProgram()
    }
}