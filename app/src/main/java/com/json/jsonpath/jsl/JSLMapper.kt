package com.json.jsonpath.jsl

import pt.rocket.utils.parser.JSLEvaluator
import pt.rocket.utils.parser.Program
import java.lang.Exception
import java.lang.RuntimeException
import kotlin.jvm.Throws

class JSLMapper {

    companion object {

        const val JSL_EMPTY_STRING = "jslStringGeneratesEmptyStatements"

        fun select(jsl: String, json: Any) : Any {
            try {
                val program = getProgram(jsl)
                val evaluator = JSLEvaluator()
                return evaluator.evaluate(json, program)
            } catch (e: Exception) {
                throw e
            }
        }

        @Throws(RuntimeException::class)
        private fun getProgram(jsl: String) : Program {
            val jslData = JSL(jsl)
            val program = jslData.compile()
            if (program.statements.size <= 0) {
                throw RuntimeException("$JSL_EMPTY_STRING : $jslData")
            }
            return program
        }
    }
}