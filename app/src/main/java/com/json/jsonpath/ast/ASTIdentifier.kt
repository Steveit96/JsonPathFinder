package com.json.jsonpath.ast

import pt.rocket.utils.parser.Token

data class ASTIdentifier(var token: Token, var value: String): ASTExpression {
    override fun tokenLiteral() = token.literal
    var description = value
}