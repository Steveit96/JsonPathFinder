package com.json.jsonpath.ast

import com.json.jsonpath.isNull
import pt.rocket.utils.parser.Token

class ASTInfixExpression(var token: Token, left: ASTExpression?, oper: String, right: ASTExpression?):
    ASTExpression {

    override fun tokenLiteral(): String {
        return token.literal
    }

    var description: String = getASTInfixExpression(left, oper, right)

    private fun getASTInfixExpression(left: ASTExpression?, oper: String, right: ASTExpression?): String {
        if (left.isNull() || right.isNull()) {
            return ""
        }

        // to replace tokenLiteranl to description
        return "${left.tokenLiteral()} $oper ${right.tokenLiteral()}"
    }
}
