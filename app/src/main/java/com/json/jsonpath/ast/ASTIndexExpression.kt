package com.json.jsonpath.ast

import com.json.jsonpath.isNull
import pt.rocket.utils.parser.Token

class ASTIndexExpression(var token: Token, private var left: ASTExpression?, private var index: ASTExpression?):
    ASTExpression {

    override fun tokenLiteral(): String {
        return token.literal
    }

    var description: String = getASTIndexDescription(left, index)

    private fun getASTIndexDescription(left: ASTExpression?, index: ASTExpression?): String {
        if (left.isNull() || index.isNull()) {
            return ""
        }
        return "(${left.tokenLiteral()})[${index.tokenLiteral()}]"
    }

    fun getLeft() = left

    fun getIndex() = index
}
