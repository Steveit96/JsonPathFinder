package pt.rocket.utils.parser

import com.json.jsonpath.ast.*
import com.json.jsonpath.isNull
import java.util.EnumMap
import kotlin.collections.ArrayList

sealed class ParserError {
    data class ParserFailed(val v1: List<String>) : ParserError()
}

// MARK: - Parser
private typealias PrefixParser = () -> ASTExpression?
private typealias InfixParser = (ASTExpression?) -> ASTExpression?

class RRParser(private var lexer: Lexer) {

    var errors: ArrayList<String> = arrayListOf()
    private var curToken: Token? = null
    private var peekToken: Token? = null
    private var prefixParseFuncs: EnumMap<TokenType, PrefixParser> = EnumMap(TokenType::class.java)
    private var infixParseFuncs: EnumMap<TokenType, InfixParser> = EnumMap(TokenType::class.java)

    private enum class Precedence(val rawValue: Int) {
        LOWEST(0),
        PREFIX(1),
        INDEX(2);

        companion object {
            operator fun invoke(rawValue: Int) = values().firstOrNull { it.rawValue == rawValue }
        }
    }

    private var precedenceMap: Map<TokenType, Precedence> = mapOf(TokenType.LBRACKET to Precedence.INDEX)

    init {
        registerPrefix(::parseSelectExpression, TokenType.DOT)
        registerPrefix(::parseIdentifier, TokenType.IDENT)
        registerPrefix(::parseIntegerLiteral, TokenType.INT)
        registerPrefix(::parsePrefixExpression, TokenType.MINUS)
        registerInfix(::parseIndexExpression, TokenType.LBRACKET)
        this.nextToken()
        this.nextToken()
    }

    // / An index expression is represented by the following grammar
    // / [<integer literal>]
    private fun parseIndexExpression(left: ASTExpression?): ASTExpression? {
        val tok = curToken
        if (!expectPeek(TokenType.INT)) {
            return null
        }
        val idx = parseExpression(Precedence.LOWEST)
        if (!expectPeek(TokenType.RBRACKET)) {
            return null
        }
        return ASTIndexExpression(token = tok!!, left = left, index = idx)
    }

    fun parseProgram(): Program {
        val program = Program()
        while (!isCurToken(TokenType.EOF)) {
            val stmt = parseStatement()
            if (stmt.isNull()) {
                nextToken()
                continue
            }
            program.statements.add(stmt)
            nextToken()
        }
        if (errors.isNotEmpty()) {
            ParserError.ParserFailed(errors)
        }
        return program
    }

    // MARK: Token Handling
    private fun nextToken() {
        curToken = peekToken
        peekToken = lexer.nextToken()
    }

    private fun isCurToken(type: TokenType) = curToken!!.type == type

    private fun isPeekToken(type: TokenType) = peekToken!!.type == type

    private fun expectPeek(type: TokenType): Boolean {
        return if (isPeekToken(type)) {
            nextToken()
            true
        } else {
            peekError(type)
            false
        }
    }

    private fun peekError(type: TokenType) {
        errors.add("expected next token to be $type, got ${peekToken!!.type} instead")
    }

    private fun noPrefixParseFuncErr(type: String) {
        errors.add("prefix parse func for $type not found")
    }

    // MARK: Precedence
    private fun peekPrecedence(): Precedence {
        return precedenceMap[peekToken!!.type] ?: return Precedence.LOWEST
    }

    // MARK: Parsing
    private fun parseStatement(): ASTStatement =
        // JSL only has one type of statement right now.
        parseExpressionStatement()

    private fun parseExpressionStatement(): ASTExpressionStatement =
        ASTExpressionStatement(token = curToken!!, expression = parseExpression(Precedence.LOWEST))

    private fun parseExpression(precedence: Precedence): ASTExpression? {
        val fn = prefixParseFuncs[curToken!!.type]
        if (fn == null) {
            noPrefixParseFuncErr(curToken!!.literal)
            return null
        }
        var leftExpr = fn()
        while (!isPeekToken(TokenType.DOT) && precedence.rawValue < peekPrecedence().rawValue) {
            val infix2 = infixParseFuncs[peekToken!!.type] ?: return leftExpr
            nextToken()
            leftExpr = infix2(leftExpr)
        }
        return leftExpr
    }

    private fun parsePrefixExpression(): ASTExpression {
        val tok = curToken
        val lit = curToken!!.literal
        nextToken()
        return ASTPrefixExpression(token = tok!!, oper = lit, right = parseExpression(Precedence.PREFIX))
    }

    // A select expression is represented by the following grammar
    // .<identifier>
    private fun parseSelectExpression(): ASTExpression? {
        val tok = curToken
        if (!expectPeek(TokenType.IDENT)) {
            return null
        }
        return ASTSelectExpression(token = tok!!, key = curToken!!.literal)
    }

    private fun parseIntegerLiteral(): ASTExpression? {
        val value = curToken!!.literal.toInt()
        if (value.isNull()) {
            errors.add("could not parse ${curToken!!.literal} as integer")
            return null
        }
        return ASTIntegerLiteral(token = curToken!!, value = value)
    }

    private fun parseIdentifier(): ASTExpression =
        ASTIdentifier(token = curToken!!, value = curToken!!.literal)

    private fun registerPrefix(fn: PrefixParser, type: TokenType) {
        prefixParseFuncs[type] = fn
    }

    private fun registerInfix(fn: InfixParser, type: TokenType) {
        infixParseFuncs[type] = fn
    }
}