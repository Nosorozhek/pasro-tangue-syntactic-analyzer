package org.example

import kotlinx.serialization.json.Json
import kotlin.time.measureTimedValue

fun main() {
    val file = object {}.javaClass.getResource("/example.pt")
    val code = file?.readText() ?: ""
    val (tokens, lexingTime) = measureTimedValue {
        val lexer = Lexer(code)
         lexer.tokenize()
    }
    for (token in tokens) {
        println(token)
    }
    val (ast, parsingTime) = measureTimedValue {
        val parser = Parser(tokens)
        parser.parse()
    }
    val json = Json {
        classDiscriminator = "class"
        prettyPrint = true
    }
    println(json.encodeToString(ast))
    println("number of tokens: ${tokens.size}")
    println("lexed in $lexingTime")
    println("parsed in $parsingTime")
}