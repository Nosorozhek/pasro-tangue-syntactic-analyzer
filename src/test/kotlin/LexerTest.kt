import org.example.Lexer
import org.example.Token
import org.example.TokenType
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals


class LexerTest {
    private fun lexer(input: String): Lexer = Lexer(input, StubErrorLogger())
    private fun tokenize(input: String): List<Token> = lexer(input).tokenize().toList()


    @Test
    fun `tokenize simple function`() {
        val input = """
            fun main() {
                return 42;
            }
        """.trimIndent()
        val tokens = tokenize(input)
        val expected = listOf(
            Token(TokenType.FUN, "fun", 0, 1, 1),
            Token(TokenType.IDENTIFIER, "main", 4, 1, 5),
            Token(TokenType.PAREN_OPEN, "(", 8, 1, 9),
            Token(TokenType.PAREN_CLOSE, ")", 9, 1, 10),
            Token(TokenType.BRACE_OPEN, "{", 11, 1, 12),
            Token(TokenType.RETURN, "return", 17, 2, 5),
            Token(TokenType.NUMBER, "42", 24, 2, 12),
            Token(TokenType.SEMICOLON, ";", 26, 2, 14),
            Token(TokenType.BRACE_CLOSE, "}", 28, 3, 1),
            Token(TokenType.EOF, "", 29, 3, 2),
        )
        assertContentEquals(expected, tokens)
    }

    @Test
    fun `tokenize function with arguments`() {
        val input = """
            fun min(a: int, b: int): int {}
        """.trimIndent()
        val tokens = tokenize(input).map { it.value }
        val expected = listOf("fun", "min", "(", "a", ":", "int", ",", "b", ":", "int", ")", ":", "int", "{", "}", "")
        assertContentEquals(expected, tokens)
    }

    @Test
    fun `tokenize if else statement`() {
        val input = """
            if (a < b) {
                return a;
            } else {
                return b;
            }
        """.trimIndent()
        val tokens = tokenize(input).map { it.value }
        val expected = listOf(
            "if", "(", "a", "<", "b", ")", "{", "return", "a", ";", "}", "else", "{", "return", "b", ";", "}", ""
        )
        assertContentEquals(expected, tokens)
    }

    @Test
    fun `tokenize string`() {
        val input = """
            fun main() {
                return "Hello, World!";
            }
        """.trimIndent()
        val tokens = tokenize(input).map { it.value }
        val expected = listOf("fun", "main", "(", ")", "{", "return", "Hello, World!", ";", "}", "")
        assertContentEquals(expected, tokens)
    }

    @Test
    fun `tokenize operators`() {
        val input = """
            -a + b - c * d / e % f == g != h <= i >= j < k > l = m
        """.trimIndent()
        val tokens = tokenize(input).map { it.value }
        val expected = listOf(
            "-", "a", "+", "b", "-", "c", "*", "d", "/", "e", "%", "f", "==",
            "g", "!=", "h", "<=", "i", ">=", "j", "<", "k", ">", "l", "=", "m", ""
        )
        assertContentEquals(expected, tokens)
    }

    @Test
    fun `tokenize operators without whitespaces`() {
        val input = """
            -a + b - c * d / e % f == g != h <= i >= j < k > l = m
        """.trimIndent().filter { it != ' ' }
        val tokens = tokenize(input).map { it.value }
        val expected = listOf(
            "-", "a", "+", "b", "-", "c", "*", "d", "/", "e", "%", "f", "==",
            "g", "!=", "h", "<=", "i", ">=", "j", "<", "k", ">", "l", "=", "m", ""
        )
        assertContentEquals(expected, tokens)
    }

    @Test
    fun `tokenize without whitespaces`() {
        val input = """
            fun min(int a,int b):int{if(a<b){return a;}return b;}
            fun main(){int x=12;int y=13;print(x+y/12)return min(x,y)!=12;}
        """.trimIndent()
        val tokens = tokenize(input).map { it.value }
        val expected = listOf(
            "fun", "min", "(", "int", "a", ",", "int", "b", ")", ":", "int", "{",
            "if", "(", "a", "<", "b", ")", "{", "return", "a", ";", "}",
            "return", "b", ";",
            "}",
            "fun", "main", "(", ")", "{",
            "int", "x", "=", "12", ";",
            "int", "y", "=", "13", ";",
            "print", "(", "x", "+", "y", "/", "12", ")",
            "return", "min", "(", "x", ",", "y", ")", "!=", "12", ";",
            "}", ""
        )
        assertContentEquals(expected, tokens)
    }

    @Test
    fun `tokenize identifiers`() {
        val input = """
            func fun if if1 else else1 else_ return returnn
        """.trimIndent()
        val tokens = tokenize(input).map { it.value to it.type }
        val expected = listOf(
            "func" to TokenType.IDENTIFIER,
            "fun" to TokenType.FUN,
            "if" to TokenType.IF,
            "if1" to TokenType.IDENTIFIER,
            "else" to TokenType.ELSE,
            "else1" to TokenType.IDENTIFIER,
            "else_" to TokenType.IDENTIFIER,
            "return" to TokenType.RETURN,
            "returnn" to TokenType.IDENTIFIER,
            "" to TokenType.EOF,
        )
        assertContentEquals(expected, tokens)
    }

    @Test
    fun `tokenize comments`() {
        val input = """
            // just comment
            fun main() {
                // another comment
                return 42; // inline comment
            }
        """.trimIndent()
        val tokens = tokenize(input).map { it.value }
        val expected = listOf("fun", "main", "(", ")", "{", "return", "42", ";", "}", "")
        assertContentEquals(expected, tokens)
    }
}
