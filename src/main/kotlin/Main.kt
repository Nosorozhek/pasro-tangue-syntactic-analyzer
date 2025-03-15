package org.example

import kotlinx.serialization.json.Json
import kotlin.time.measureTimedValue

fun main() {
    for (path in listOf("/example.pt", "/lexer_error.pt", "/parser_error.pt")) {
        try {
            println("Parsing file $path...")
            parse(path)
        } catch (e: IllegalArgumentException) {
            println("Failed to parse file $path: $e" + System.lineSeparator())
        }
    }
}

fun parse(path: String) {
    val file = object {}.javaClass.getResource(path)!!
    val code = file.readText()
    val logger = DefaultErrorLogger(code, file)
    val (tokens, lexingTime) = measureTimedValue {
        val lexer = Lexer(code, logger)
        lexer.tokenize()
    }
    val (ast, parsingTime) = measureTimedValue {
        val parser = Parser(tokens, logger)
        parser.parse()
    }
    val json = Json {
        classDiscriminator = "class"
        prettyPrint = true
    }

    println(
        json.encodeToString(ast) + """
        
    ^~~~ Parsed file $path
    Number of tokens: ${tokens.size}
    Lexed in $lexingTime
    Parsed in $parsingTime
    
    """.trimIndent()
    )
}
