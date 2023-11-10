package syntatic;

import static error.LanguageException.Error.InvalidLexeme;
import static error.LanguageException.Error.UnexpectedEOF;
import static error.LanguageException.Error.UnexpectedLexeme;

import error.LanguageException;
import lexical.LexicalAnalysis;
import lexical.Token;

public class SyntaticAnalysis {

    private LexicalAnalysis lex;
    private Token current;
    private Token previous;

    public SyntaticAnalysis(LexicalAnalysis lex) {
        this.lex = lex;
        this.current = lex.nextToken();
        this.previous = null;
    }

    public void process() {
        procDtd();
        eat(Token.Type.END_OF_FILE);
    }

    private void advance() {
        // System.out.println("Found " + current);
        previous = current;
        current = lex.nextToken();
    }

    private void eat(Token.Type type) {
        if (type == current.type) {
            advance();
        } else {
            // System.out.println("Expected (..., " + type + ", ..., ...), found " + current);
            reportError();
        }
    }

    private boolean check(Token.Type ...types) {
        for (Token.Type type : types) {
            if (current.type == type)
                return true;
        }

        return false;
    }

    private boolean match(Token.Type ...types) {
        if (check(types)) {
            advance();
            return true;
        } else {
            return false;
        }
    }

    private void reportError() {
        int line = current.line;
        switch (current.type) {
            case INVALID_TOKEN:
                throw LanguageException.instance(line, InvalidLexeme, current.lexeme);
            case UNEXPECTED_EOF:
            case END_OF_FILE:
                throw LanguageException.instance(line, UnexpectedEOF);
            default:
                throw LanguageException.instance(line, UnexpectedLexeme, current.lexeme);
        }
    }

    private void procDtd() {
        eat(Token.Type.DOCTYPE);
        procDoctype();
    }

    private void procDoctype() {

        eat(Token.Type.NAME);
        eat(Token.Type.OPEN_BRA);
        
        while (check(Token.Type.ELEMENT, Token.Type.ATTLIST)) {
            if(check(Token.Type.ELEMENT)) {
                procElement();
            } else if (check(Token.Type.ATTLIST)) {
                procAttlist();
            } else {
                reportError();
            }

        }

        eat(Token.Type.CLOSE_BRA);
        eat(Token.Type.GRATER);
    }

    private void procElement() {
        eat(Token.Type.ELEMENT);
        eat(Token.Type.NAME);
        
        if (check(Token.Type.EMPTY, Token.Type.ANY)) {
            procCategoria();
        } else if (match(Token.Type.PCDATA, Token.Type.NAME, Token.Type.OPEN_PAR)) {
            do {
                procFilho();
            } while (match(Token.Type.BAR));
            match(Token.Type.CLOSE_PAR);
        } else {
            reportError();
        }

        eat(Token.Type.GRATER);
    }

    private void procAttlist() {
        eat(Token.Type.ATTLIST);
        eat(Token.Type.NAME);

        while (match(Token.Type.NAME)) {
            procTipo();
            procValorAtributo();
        }

        eat(Token.Type.GRATER);
    }

    private void procFilho() { //não entendi qq precisa fazer 
        if (match(Token.Type.PCDATA, Token.Type.NAME)) {
            switch(previous.type) {
                case PCDATA:
                    //do nothing.
                    break;
                case NAME:
                    match(Token.Type.ASTERISK);
                    match(Token.Type.QUESTION_MARK);
                    match(Token.Type.PLUS);
                    break;
                default:
                    reportError();
                    break;
            }
        }
    }

    private void procTipo() {
        if (match(Token.Type.CDATA, Token.Type.ID, Token.Type.NAME, 
                    Token.Type.OPEN_PAR)) {
            switch(previous.type) {
                case CDATA:
                    //do nothing.
                    break;
                case ID:
                    //do nothing.
                    break;
                case NAME:
                    //do nothing.
                    break;
                case OPEN_PAR:
                    eat(Token.Type.NAME);
                    do {
                        eat(Token.Type.BAR);
                        eat(Token.Type.NAME);
                    } while (check(Token.Type.BAR));
                    eat(Token.Type.CLOSE_PAR);
                    break;
                default:
                    reportError();
                    break;
            }
        }
    }

    private void procValorAtributo() {
        if(check(Token.Type.STRING_LITERAL)) {
            procValor();
        } else if(match(Token.Type.REQUIRED, Token.Type.IMPLIED, 
                    Token.Type.FIXED)) {
            switch(previous.type) {
                case REQUIRED:
                    //do nothing.
                    break;
                case IMPLIED:
                    //do nothing.
                    break;
                case FIXED:
                    check(Token.Type.STRING_LITERAL);
                    procValor();
                    break;
                default:
                    reportError();
                    break;
            }
        }
    }

    private void procValor() {
        eat(Token.Type.STRING_LITERAL);
    }

    private void procCategoria() {
        if(match(Token.Type.EMPTY, Token.Type.ANY)) {
            switch(previous.type) {
                case EMPTY:
                    //do nothing.
                    break;
                case ANY:
                    //do nothing.
                    break;
                default:
                    reportError();
                    break;
            }
        }
    }

}
