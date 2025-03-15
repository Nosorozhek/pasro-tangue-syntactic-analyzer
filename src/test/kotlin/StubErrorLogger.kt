import org.example.ErrorLogger

internal class StubErrorLogger : ErrorLogger {
    override fun logError(position: Int, line: Int, column: Int, message: String) {}
}
