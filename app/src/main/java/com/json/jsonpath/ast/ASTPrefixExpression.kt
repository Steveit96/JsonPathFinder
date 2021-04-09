package com.json.jsonpath.ast

import com.json.jsonpath.isNull
import pt.rocket.utils.parser.Token

class ASTPrefixExpression(var token: Token, private var oper:String, private var right: ASTExpression?):
    ASTExpression {

    override fun tokenLiteral(): String {
        return token.literal
    }

    var description: String = getASTPrefixDescription(oper, right)

    private fun getASTPrefixDescription(oper:String, right: ASTExpression?) : String {
        if (right.isNull()) {
            return ""
        }
        // Need to replace with des
        return "$oper ${right.tokenLiteral()}"
    }

    fun getRight() = right

    fun getOper() = oper
}