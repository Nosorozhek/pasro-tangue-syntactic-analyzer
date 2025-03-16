package org.example

import kotlinx.serialization.json.Json
import java.io.File
import kotlin.time.measureTimedValue

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        default()
        return
    }

    var output: String? = null
    val input = args.mapIndexed { i, arg ->
        if (arg == "-o") {
            output = args.getOrNull(i + 1)
            null
        } else {
            arg
        }
    }.firstOrNull()

    if (input == null || args.size !in listOf(1, 3)) {
        println("Usage: pt-compiler input.pt [-o output.json]")
        return
    }

    try {
        parse(input, output)
    } catch (e: IllegalArgumentException) {
        println("Failed to parse file $input: $e")
    }
}

fun default() {
    for (path in listOf("example.pt", "lexer_error.pt", "parser_error.pt")) {
        try {
            println("Parsing file $path...")
            parse("./src/main/resources/$path")
        } catch (e: IllegalArgumentException) {
            println("Failed to parse file $path: $e" + System.lineSeparator())
        }
    }
}

fun parse(path: String, output: String? = null) {
    val file = File(path)
    val code = file.readText()
    val logger = DefaultErrorLogger(code, file)
    val (ast, parsingTime) = measureTimedValue {
        val lexer = Lexer(code, logger)
        val tokens = lexer.tokenize()
        val parser = Parser(tokens, logger)
        parser.parse()
    }
    val json = Json {
        classDiscriminator = "class"
        prettyPrint = true
    }

    if (output != null) {
        File(output).writeText(json.encodeToString(ast))
    } else {
        println(json.encodeToString(ast) + System.lineSeparator() + "^~~~ Parsed file $path")
    }

    println("Parsed file ${file.name} in $parsingTime")
}
