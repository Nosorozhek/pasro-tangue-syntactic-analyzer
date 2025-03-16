import org.example.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class ParserTest {
    private fun lexer(input: String): Lexer = Lexer(input, StubErrorLogger())
    private fun parser(tokens: Sequence<Token>): Parser = Parser(tokens, StubErrorLogger())

    private fun function(name: String, type: String, args: List<ArgumentNode>, vararg statements: Node): ProgramNode =
        ProgramNode(
            listOf(FunctionDeclarationNode(name, args, type, BlockNode(statements.toList())))
        )

    private fun function(name: String, vararg args: Node): FunctionCallNode = FunctionCallNode(name, args.toList())

    private fun main(vararg statements: Node): ProgramNode = function("main", "void", listOf(), *statements)

    private fun arg(name: String, type: String): ArgumentNode = ArgumentNode(name, type)

    private fun block(vararg statements: Node): BlockNode = BlockNode(statements.toList())

    private fun ret(value: Node): ReturnNode = ReturnNode(value)

    private fun variable(name: String): VariableNode = VariableNode(name)
    private fun variable(name: String, type: String, value: Node?): VariableDeclarationNode =
        VariableDeclarationNode(name, type, value)

    private fun number(value: Int): NumberLiteralNode = NumberLiteralNode(value)
    private fun string(value: String): StringLiteralNode = StringLiteralNode(value)

    private fun minus(node: Node): PrefixOperatorNode = PrefixOperatorNode("-", node)
    private fun op(name: String, left: Node, right: Node): BinaryOperatorNode = BinaryOperatorNode(name, left, right)

    private fun parse(input: String): Node {
        val tokens = lexer(input).tokenize()
        return parser(tokens).parse()
    }

    @Test
    fun `parse simple function`() {
        val input = """
            fun main() {
                return 42;
            }
        """.trimIndent()
        val ast = parse(input)
        val expected = main(ret(number(42)))
        assertEquals(expected, ast)
    }

    @Test
    fun `parse function call`() {
        val input = """
            fun main() {
                print(min(42, 12), max(42, 12));
            }
        """.trimIndent()
        val ast = parse(input)
        val expected = main(
            function(
                "print",
                function("min", number(42), number(12)),
                function("max", number(42), number(12)),
            )
        )
        assertEquals(expected, ast)
    }

    @Test
    fun `parse function with arguments`() {
        val input = """
            fun min(a: int, b: int, c: string): int {}
        """.trimIndent()
        val ast = parse(input)
        val expected =
            function(
                "min", "int",
                listOf(
                    arg("a", "int"),
                    arg("b", "int"),
                    arg("c", "string")
                )
            )
        assertEquals(expected, ast)
    }

    @Test
    fun `parse variable declaration`() {
        val input = """
            fun main() {
                int x = 0;
                string s;
                string t = "42\n";
            }
        """.trimIndent()
        val ast = parse(input)
        val expected = main(
            variable("x", "int", number(0)),
            variable("s", "string", null),
            variable("t", "string", string("42\\n"))
        )
        assertEquals(expected, ast)
    }

    @Test
    fun `parse if else statement`() {
        val input = """
            fun main() {
                if (a < b) {
                    return a;
                } else {
                    return b;
                }
            }
        """.trimIndent()
        val ast = parse(input)
        val expected = main(
            IfNode(
                op("<", variable("a"), variable("b")),
                block(ret(variable("a"))),
                block(ret(variable("b"))),
            )
        )
        assertEquals(expected, ast)
    }

    @Test
    fun `parse string`() {
        val input = """
            fun main() {
                return "Hello, World!";
            }
        """.trimIndent()
        val ast = parse(input)
        val expected = main(
            ret(string("Hello, World!"))
        )
        assertEquals(expected, ast)
    }

    @Test
    fun `parse operators`() {
        val input = """
            fun main() {
                a + c * -d / e - -f;
            }
        """.trimIndent()
        val ast = parse(input)
        val expected = main(
            op(
                "-", op(
                    "+", variable("a"),
                    op(
                        "/",
                        op("*", variable("c"), minus(variable("d"))),
                        variable("e")
                    )
                ),
                minus(variable("f"))
            )
        )
        assertEquals(expected, ast)
    }

    @Test
    fun `parse operators2`() {
        val input = """
            fun main() {
                a + c <= --d / e != --2;
            }
        """.trimIndent()
        val ast = parse(input)
        val expected = main(
            op(
                "!=",
                op(
                    "<=",
                    op("+", variable("a"), variable("c")),
                    op(
                        "/",
                        minus(minus(variable("d"))),
                        variable("e")
                    )
                ),
                minus(minus(number(2)))
            )
        )
        assertEquals(expected, ast)
    }
}
