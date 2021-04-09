package com.json.jsonpath.ast

import pt.rocket.utils.parser.Token

class ASTIntegerLiteral(var token: Token, var value: Int): ASTExpression {
    override fun tokenLiteral(): String {
        return token.literal
    }

    var description: String = value.toString()
}