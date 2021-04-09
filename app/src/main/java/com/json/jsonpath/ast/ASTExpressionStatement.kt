package com.json.jsonpath.ast

import com.json.jsonpath.isNull
import pt.rocket.utils.parser.Token

class ASTExpressionStatement(var token: Token, private var expression: ASTExpression?):
    ASTStatement {

    var description: String = getDescription(expression)

    override fun tokenLiteral(): String {
        return token.literal
    }

    private fun getDescription(expression: ASTExpression?): String {
        if (expression.isNull()) {
            return ""
        }
        return expression.tokenLiteral()
    }

    fun getExpression() = expression
}
