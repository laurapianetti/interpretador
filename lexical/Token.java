package lexical;

public class Token {

    public static enum Type {
        // Specials.
        INVALID_TOKEN,
        UNEXPECTED_EOF,
        END_OF_FILE,

        // Symbols.
        GRATER,
        OPEN_PAR,
        CLOSE_PAR,
        OPEN_BRA,
        CLOSE_BRA,
        BAR,
        ASTERISK,
        QUESTION_MARK,
        PLUS,

        // Keywords.
        DOCTYPE,
        ELEMENT,
        ATTLIST,
        ANY,
        EMPTY,
        PCDATA,
        REQUIRED,
        IMPLIED,
        FIXED,
        CDATA,
        ID,

        // Others.
        NAME,              // identifier
        INTEGER_LITERAL,   // integer literal
        FLOAT_LITERAL,     // float literal
        CHAR_LITERAL,      // char literal
        STRING_LITERAL     // string literal

    };

    public String lexeme;
    public Type type;
    public int line;

    public Token(String lexeme, Type type) {
        this.lexeme = lexeme;
        this.type = type;
        this.line = 0;
    }

    public String toString() {
        return new StringBuffer()
            .append("(\"")
            .append(this.lexeme)
            .append("\", ")
            .append(this.type)
            .append(", ")
            .append(this.line)
            .append(")")
            .toString();
    }

}
