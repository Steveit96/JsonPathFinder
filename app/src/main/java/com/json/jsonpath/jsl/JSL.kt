package com.json.jsonpath.jsl

import pt.rocket.utils.parser.Lexer
import pt.rocket.utils.parser.Program
import pt.rocket.utils.parser.RRParser

data class JSL(val input: String) {

    fun compile(): Program {
        val lexer = Lexer(input)
        val parser = RRParser(lexer)
        return parser.parseProgram()
    }
}