package org.example

import java.net.URL

interface ErrorLogger {
    fun logError(position: Int, line: Int, column: Int, message: String)
}

class DefaultErrorLogger(private val code: String, private val file: URL) : ErrorLogger {
    override fun logError(position: Int, line: Int, column: Int, message: String) {
        var start = position
        while (start >= 0 && code[start] != '\n') --start
        ++start

        var end = position
        while (end < code.length && code[end] != '\n') ++end
        println(
            code.substring(start, end) + System.lineSeparator()
                    + " ".repeat(position - start) +
                    "^~~~ Error: $message at file://${file.file}:$line:$column"
        )
    }
}