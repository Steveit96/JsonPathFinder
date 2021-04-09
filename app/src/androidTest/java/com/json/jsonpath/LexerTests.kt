package pt.rocket.utils.parser

import org.junit.Assert.assertEquals
import org.junit.Test

class LexerTests {

    @Test
    fun testNextTokenWithOneKey() {
        val input = ".data"

        val testCases: Array<Token> = arrayOf(
            Token(TokenType.DOT, "."),
            Token(TokenType.IDENT, "data"),
            Token(TokenType.EOF, "")
        )

        val l = Lexer(input)
        var tok: Token

        for (expected in testCases) {
            tok = l.nextToken()
            assertEquals(expected, tok)
        }
    }

    @Test
    fun testNextTokenWithMultipleKeys() {
        val input = ".data.items[]"

        val testCases: Array<Token> = arrayOf(
            Token(TokenType.DOT, "."),
            Token(TokenType.IDENT, "data"),
            Token(TokenType.DOT, "."),
            Token(TokenType.IDENT, "items"),
            Token(TokenType.LBRACKET, "["),
            Token(TokenType.RBRACKET, "]"),
            Token(TokenType.EOF, ""),
        )

        val l = Lexer(input)
        var tok: Token

        for (expected in testCases) {
            tok = l.nextToken()
            assertEquals(expected, tok)
        }
    }

    @Test
    fun testNextTokenWithIndexedKeys() {
        val input = ".data.items[2].image"

        val testCases: Array<Token> = arrayOf(
            Token(TokenType.DOT, "."),
            Token(TokenType.IDENT, "data"),
            Token(TokenType.DOT, "."),
            Token(TokenType.IDENT, "items"),
            Token(TokenType.LBRACKET, "["),
            Token(TokenType.INT, "2"),
            Token(TokenType.RBRACKET, "]"),
            Token(TokenType.DOT, "."),
            Token(TokenType.IDENT, "image"),
            Token(TokenType.EOF, ""),
        )

        val l = Lexer(input)
        var tok: Token

        for (expected in testCases) {
            tok = l.nextToken()
            assertEquals(expected, tok)
        }
    }

    @Test
    fun testNextTokenWithIndexNested() {
        val input = ".data.items[2].image.url[0]"

        val testCases: Array<Token> = arrayOf(
            Token(TokenType.DOT, "."),
            Token(TokenType.IDENT, "data"),
            Token(TokenType.DOT, "."),
            Token(TokenType.IDENT, "items"),
            Token(TokenType.LBRACKET, "["),
            Token(TokenType.INT, "2"),
            Token(TokenType.RBRACKET, "]"),
            Token(TokenType.DOT, "."),
            Token(TokenType.IDENT, "image"),
            Token(TokenType.DOT, "."),
            Token(TokenType.IDENT, "url"),
            Token(TokenType.LBRACKET, "["),
            Token(TokenType.INT, "0"),
            Token(TokenType.RBRACKET, "]"),
            Token(TokenType.EOF, ""),
        )

        val l = Lexer(input)
        var tok: Token
        for (expected in testCases) {
            tok = l.nextToken()
            assertEquals(expected, tok)
        }
    }
}