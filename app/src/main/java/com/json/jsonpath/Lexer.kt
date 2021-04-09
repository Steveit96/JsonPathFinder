package pt.rocket.utils.parser

import com.json.jsonpath.isNull


// / Lexer represents the lexical analyzer for JSON Selector Language
class Lexer(inputString: String) {

    private var input = charArrayOf()
    private var position: Int = 0
    private var readPosition: Int = 0
    private var currentCharacter: Char? = null

    init {
        input = inputString.toCharArray()
        readChar()
    }

    // / nextToken returns the next lexed token for the given input
    fun nextToken(): Token {

        if (currentCharacter.isNull()) return Token(TokenType.EOF, "")

        val token = when (val ch = currentCharacter as Char) {
            '.' -> Token(TokenType.DOT, ch.toString())
            '[' -> Token(TokenType.LBRACKET, ch.toString())
            ']' -> Token(TokenType.RBRACKET, ch.toString())
            else -> {
                if (ch.isDigit()) {
                    return Token(TokenType.INT, readNumber())
                }
                return readIdentifier()
            }
        }

        readChar()

        return token
    }

    // readNumber returns a token with a numeric literal
    private fun readNumber(): String {
        val startIdx = position
        while (currentCharacter != null && (currentCharacter as Char).isDigit()) {
            readChar()
        }
        return input.concatToString(startIdx, position)
    }

    // readChar reads one character
    // Note that only ASCII is supported.
    private fun readChar() {
        currentCharacter = if (readPosition >= input.size) {
            null
        } else {
            input[readPosition]
        }

        position = readPosition
        readPosition += 1
    }

    // readIdentifier returns a token with an identifier as the literal
    private fun readIdentifier(): Token {
        val startIdx = position
        while (isIdent(currentCharacter)) {
            readChar()
        }

        val str = input.concatToString(startIdx, position)

        return Token(TokenType.IDENT, str)
    }

    // isIdent returns true if a character is not one of the other tokens
    private fun isIdent(ch: Char?): Boolean {
        return if (ch.isNull()) {
            false
        } else {
            (ch == '.').not() && (ch == '[').not() && (ch == ']').not()
        }
    }
}