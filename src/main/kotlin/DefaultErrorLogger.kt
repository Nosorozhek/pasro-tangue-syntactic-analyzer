package org.example

import java.io.File

interface ErrorLogger {
    fun logError(position: Int, line: Int, column: Int, message: String)
}

class DefaultErrorLogger(private val code: String, private val file: File) : ErrorLogger {
    override fun logError(position: Int, line: Int, column: Int, message: String) {
        var start = position - 1
        while (start >= 0 && code[start] != '\n') --start

        var end = position
        while (end < code.length && code[end] != '\n') ++end
        println(
            code.substring(start, end) + System.lineSeparator()
                    + " ".repeat(position - start - 1) +
                    "^~~~ Error: $message at file://${file.absolutePath}:$line:$column"
        )
    }
}
