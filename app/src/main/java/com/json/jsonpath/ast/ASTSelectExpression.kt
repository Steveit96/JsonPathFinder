package com.json.jsonpath.ast

import pt.rocket.utils.parser.Token

class ASTSelectExpression(var token: Token, var key: String): ASTExpression {

    override fun tokenLiteral() = token.literal

    var description: String = "${token.literal} $key"
}