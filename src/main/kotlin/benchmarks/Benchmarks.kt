package benchmarks

import org.example.ErrorLogger
import org.example.Lexer
import org.example.Node
import org.example.Parser
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

private class StubErrorLogger : ErrorLogger {
    override fun logError(position: Int, line: Int, column: Int, message: String) {}
}

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@Warmup(iterations = 5)
@Measurement(iterations = 5, timeUnit = TimeUnit.MILLISECONDS)
open class JmhBenchmarks {
    private lateinit var input: String

    @Setup(Level.Trial)
    fun setUp() {
        input = (1..100000).joinToString(" ") {
            """
                fun test${it}(a: int, b: int) {
                   print((a - b) * (a - b) / 2 + 12 * (a + b) % 7);
                   if (a < b) {
                       return a;
                   } else {
                       a = b * -1;
                       return a;
                   }
                }
            """.trimIndent()
        }
    }

    @Benchmark
    fun lexerBenchmark(): Int {
        val tokens = Lexer(input, StubErrorLogger()).tokenize().toList()
        return tokens.size
    }

    @Benchmark
    fun parserBenchmark(): Node {
        val tokens = Lexer(input, StubErrorLogger()).tokenize()
        return Parser(tokens, StubErrorLogger()).parse()
    }
}
