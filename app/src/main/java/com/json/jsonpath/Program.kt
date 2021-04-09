package pt.rocket.utils.parser

import com.json.jsonpath.ast.ASTNode

class Program: ASTNode {

    var statements = arrayListOf<ASTNode>()

    override fun tokenLiteral(): String {
        return if (statements.count() > 0) {
            statements.first().tokenLiteral()
        } else {
            ""
        }
    }
}