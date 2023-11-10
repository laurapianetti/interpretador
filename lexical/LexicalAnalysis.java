package lexical;

import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.HashMap;
import java.util.Map;

//import javax.security.auth.kerberos.KerberosCredMessage;

import error.InternalException;
//import lexical.Token.Type;

public class LexicalAnalysis implements AutoCloseable {

    private int line;
    private PushbackInputStream input;
    private static Map<String, Token.Type> keywords;

    static {
        keywords = new HashMap<String, Token.Type>();

        // SYMBOLS
        keywords.put(">", Token.Type.GRATER);
        keywords.put("(", Token.Type.OPEN_PAR);
        keywords.put(")", Token.Type.CLOSE_PAR);
        keywords.put("[", Token.Type.OPEN_BRA);
        keywords.put("]", Token.Type.CLOSE_BRA);
        keywords.put("|", Token.Type.BAR);
        keywords.put("*", Token.Type.ASTERISK);
        keywords.put("?", Token.Type.QUESTION_MARK);
        keywords.put("+", Token.Type.PLUS);

        // KEYWORDS
        keywords.put("<!DOCTYPE", Token.Type.DOCTYPE);
        keywords.put("<!ELEMENT", Token.Type.ELEMENT);
        keywords.put("<!ATTLIST", Token.Type.ATTLIST);
        keywords.put("ANY", Token.Type.ANY);
        keywords.put("EMPTY", Token.Type.EMPTY);
        keywords.put("#PCDATA", Token.Type.PCDATA);
        keywords.put("#REQUIRED", Token.Type.REQUIRED);
        keywords.put("#IMPLIED", Token.Type.IMPLIED);
        keywords.put("#FIXED", Token.Type.FIXED);
        keywords.put("CDATA", Token.Type.CDATA);
        keywords.put("ID", Token.Type.ID);

    }

    public LexicalAnalysis(InputStream is) {
        input = new PushbackInputStream(is);
        line = 1;
    }

    public void close() {
        try {
            input.close();
        } catch (Exception e) {
            throw new InternalException("Unable to close file");
        }
    }

    public int getLine() {
        return this.line;
    }

    public Token nextToken() {
        Token token = new Token("", Token.Type.END_OF_FILE);

        int state = 1;
        while (state != 7 && state != 8 && state != 9) {
            int c = getc();
            // System.out.printf("  [%02d, %03d ('%c')]\n",
            //     state, c, (char) c);

            switch (state) {
                case 1:
                    if (c == ' ' || c == '\t' || c == '\r') {
                        state = 1;
                    } else if (c == '\n') {
                        this.line++;
                        state = 1;
                    } else if (c == '<') {
                        token.lexeme += (char) c;
                        state = 2;
                    } else if (c == '#') {
                        token.lexeme += (char) c;
                        state = 4;
                    } else if (c == '>' || c == '(' || c == ')' ||
                                c == '[' || c == ']' || c == '|' ||
                                c == '*' || c == '?' || c == '+') {
                        token.lexeme += (char) c;
                        state = 8;
                    } else if (Character.isLetter(c)) {
                        token.lexeme += (char) c;
                        state = 5;
                    } else if (c == '\"') {
                        state = 6;
                    } else if (c == -1) {
                        token.type = Token.Type.END_OF_FILE;
                        state = 9;
                    } else {
                        token.type = Token.Type.INVALID_TOKEN;
                        state = 7;
                    }
                    break;
                case 2:
                    if (c == '!') {
                        token.lexeme += (char) c;
                        state = 3;
                    } else {
                        ungetc(c);
                        token.type = Token.Type.INVALID_TOKEN;
                        state = 7;
                    }
                    break;
                case 3:
                    if (Character.isLetter(c)) {
                        token.lexeme += (char) c;
                        state = 3;
                    } else {
                        ungetc(c);
                        state = 7;
                    }
                    break;
                case 4:
                    if (Character.isLetter(c)) {
                        token.lexeme += (char) c;
                        state = 4;
                    } else {
                        ungetc(c);
                        state = 7;
                    }
                    break;
                case 5:
                    if (Character.isLetter(c)) {
                        token.lexeme += (char) c;
                        state = 5;
                    } else {
                        ungetc(c);
                        state = 8;
                    }
                    break;
                case 6:
                    if (c == -1) {
                        token.type = Token.Type.UNEXPECTED_EOF;
                        state = 9;
                    } else if(c == '\n'){ 
                        line++;
                        state = 6;
                    } else if (c == '\"') {
                        token.type = Token.Type.STRING_LITERAL;
                        state = 9;
                    } else {
                        token.lexeme += (char) c;
                        state = 6;
                    }
                    break;
                default:
                    throw new InternalException("Unreachable");
            }
        }

        if (state == 7) // tags dtd
            token.type = keywords.containsKey(token.lexeme) ?
                keywords.get(token.lexeme) : Token.Type.INVALID_TOKEN;
        else if (state == 8) // palavras reservadas
            token.type = keywords.containsKey(token.lexeme) ?
                keywords.get(token.lexeme) : Token.Type.NAME;

        token.line = this.line;

        return token;
    }

    private int getc() {
        try {
            return input.read();
        } catch (Exception e) {
            throw new InternalException("Unable to read file");
        }
    }

    private void ungetc(int c) {
        if (c != -1) {
            try {
                input.unread(c);
            } catch (Exception e) {
                throw new InternalException("Unable to ungetc");
            }
        }
    }

}
