package pt.rocket.utils.parser

import com.json.jsonpath.ast.ASTExpressionStatement
import com.json.jsonpath.ast.ASTIndexExpression
import com.json.jsonpath.ast.ASTIntegerLiteral
import com.json.jsonpath.ast.ASTSelectExpression
import org.junit.Assert.assertEquals
import org.junit.Test

class RRParserTests {

    @Test
    fun testParseProgramWithOneStatement() {
        val input = ".data"
        val l = Lexer(input)
        val p = RRParser(l)
        val subject = p.parseProgram()
        assertEquals(1, subject.statements.size)

        val stmt = subject.statements.first().let { it as ASTExpressionStatement }
        val key = stmt.getExpression() as ASTSelectExpression
        assertEquals(stmt.tokenLiteral(), ".")
        assertEquals(key.token.literal, ".")
        assertEquals(key.key, "data")
    }

    @Test
    fun testParseProgramWithTwoStatements() {
        data class Case(var tokenLiteral: String, var identifier: String)
        val input = ".data.images"
        val l = Lexer(input)
        val p = RRParser(l)
        val subject = p.parseProgram()
        assertEquals(2, subject.statements.size)
        val testCases = arrayOf(
            Case(".", "data"),
            Case(".", "images"),
        )
        for (i in 0..1) {
            val stmt = subject.statements[i] as ASTExpressionStatement
            val key = stmt.getExpression() as ASTSelectExpression
            val testCase = testCases[i]
            assertEquals(key.token.literal, testCase.tokenLiteral)
            assertEquals(key.key, testCase.identifier)
        }
    }

    @Test
    fun testParseProgramWithIndexExpression() {
        val input = ".images[224]"
        val l = Lexer(input)
        val p = RRParser(l)
        val subject = p.parseProgram()
        val exprStmt = (subject.statements.first() as? ASTExpressionStatement) ?: return
        assertEquals(exprStmt.tokenLiteral(), ".")

        val expr = (exprStmt.getExpression() as? ASTIndexExpression) ?: return
        val sel = (expr.getLeft() as? ASTSelectExpression) ?: return
        val idx = (expr.getIndex() as? ASTIntegerLiteral) ?: return
        assertEquals(sel.key, "images")
        assertEquals(sel.token.type, TokenType.DOT)
        assertEquals(idx.value, 224)
        assertEquals(idx.token.type, TokenType.INT)
    }

    @Test
    fun testParseProgramWithIndexExpressionComplex() {
        val input = ".images[224].id"
        val l = Lexer(input)
        val p = RRParser(l)
        val subject = p.parseProgram()
        val exprStmt = (subject.statements[0] as? ASTExpressionStatement) ?: return

        assertEquals(exprStmt.tokenLiteral(), ".")
        val expr = (exprStmt.getExpression() as? ASTIndexExpression) ?: return
        val sel = (expr.getLeft() as? ASTSelectExpression) ?: return
        val idx = (expr.getIndex() as? ASTIntegerLiteral) ?: return

        assertEquals(sel.key, "images")
        assertEquals(sel.token.type, TokenType.DOT)
        assertEquals(idx.value, 224)
        assertEquals(idx.token.type, TokenType.INT)
        val exprStmt2 = (subject.statements[1] as? ASTExpressionStatement) ?: return

        assertEquals(exprStmt2.tokenLiteral(), ".")
        val expr2 = (exprStmt2.getExpression() as? ASTSelectExpression) ?: return

        assertEquals(expr2.token.literal, ".")
        assertEquals(expr2.key, "id")
    }

    @Test
    fun testParseProgramWithInvalidIndexExpression() {
        val input = ".data.images[something]"
        val l = Lexer(input)
        val p = RRParser(l)
        p.parseProgram()

        assertEquals(p.errors.size, 2)
        assertEquals(p.errors.first(), "expected next token to be INT, got IDENT instead")
        assertEquals(p.errors.last(), "prefix parse func for ] not found")
    }

    @Test
    fun testParseProgramWithErrors() {
        val input = "..[]" // This is invalid JSL
        val l = Lexer(input)
        val p = RRParser(l)
        p.parseProgram()

        val testCases = arrayOf(
            "expected next token to be IDENT, got DOT instead",
            "expected next token to be IDENT, got LBRACKET instead",
            "expected next token to be INT, got RBRACKET instead",
            "prefix parse func for ] not found"
        )
        for (i in 0..3) {
            assertEquals(p.errors[i], testCases[i])
        }
    }
}