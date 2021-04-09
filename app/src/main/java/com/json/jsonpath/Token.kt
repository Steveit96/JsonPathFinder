package pt.rocket.utils.parser

enum class TokenType {
    DOT,
    IDENT,
    INT,
    MINUS,
    LBRACKET,
    RBRACKET,
    EOF
}

data class Token(
    var type: TokenType,
    var literal: String
)
