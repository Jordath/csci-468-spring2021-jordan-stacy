package edu.montana.csci.csci468.tokenizer;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static edu.montana.csci.csci468.tokenizer.TokenType.*;
import static org.junit.jupiter.api.Assertions.*;

public class CatScriptTokenizerTest {

    @Test
    public void basicTokenizerTest(){
        assertTokensAre("1 + 1", INTEGER, PLUS, INTEGER, EOF);
        assertTokensAre("1 + 1", "1", "+", "1", "<EOF>");
        assertTokensAre("1   +   1", INTEGER, PLUS, INTEGER, EOF);
        assertTokensAre("1   +   1", "1", "+", "1", "<EOF>");
        assertTokensAre("1  \n +  \n 1", INTEGER, PLUS, INTEGER, EOF);
        assertTokensAre("1  \n +  \n 1", "1", "+", "1", "<EOF>");
    }

    @Test
    public void basicNumbers(){
        assertTokensAre("1", INTEGER, EOF);
        assertTokensAre( "1", "1", "<EOF>");
        assertTokensAre("1 10 234234", INTEGER, INTEGER, INTEGER, EOF);
        assertTokensAre("1 10 234234", "1", "10", "234234", "<EOF>");
    }

    @Test
    public void basicString(){
        assertTokensAre("\"asdf\"", STRING, EOF);
        assertTokensAre( "\"asdf\"", "asdf", "<EOF>");

        assertTokensAre("\"asdf\"\"asdf\"", STRING, STRING, EOF);
        assertTokensAre( "\"asdf\"\"asdf\"", "asdf", "asdf", "<EOF>");
    }

    @Test
    public void unterminatedStrings(){
        assertTokensAre("\"asdf", ERROR, EOF);
        assertTokensAre("\"asdf\"\"asdf", STRING, ERROR, EOF);
        assertTokensAre("\"asdf \"asdf\"", STRING, IDENTIFIER, ERROR, EOF);
    }

    @Test
    public void basicIdentifiers(){
        assertTokensAre("asdf", IDENTIFIER, EOF);
        assertTokensAre( "asdf", "asdf", "<EOF>");

        assertTokensAre("asdf asdf", IDENTIFIER, IDENTIFIER, EOF);
        assertTokensAre( "asdf asdf", "asdf", "asdf", "<EOF>");
    }

    @Test
    public void basicKeywords(){
        assertTokensAre("else false function for if not null print return true var",
                ELSE, FALSE, FUNCTION, FOR, IF, NOT, NULL,
                PRINT, RETURN, TRUE, VAR, EOF);
    }

    @Test
    public void basicSyntax(){
        assertTokensAre("( ) { } [ ] , . - + / * != = == > >= < <=",
                LEFT_PAREN, RIGHT_PAREN,
                LEFT_BRACE, RIGHT_BRACE,
                LEFT_BRACKET, RIGHT_BRACKET,
                COMMA, DOT, MINUS, PLUS, SLASH, STAR,
                BANG_EQUAL,
                EQUAL, EQUAL_EQUAL,
                GREATER, GREATER_EQUAL,
                LESS, LESS_EQUAL, EOF);
    }

    @Test
    public void commentsAreIgnored(){
        assertTokensAre("/ //   //// asdfasdf \"asdf\"\n" +
                        "  / // asdf",
                SLASH, SLASH, EOF);
    }

    @Test
    public void compoundTokensTokenizeProperly() {
        assertTokensAre("1+2", INTEGER, PLUS, INTEGER, EOF);
        assertTokensAre("\"\".\"\"", STRING, DOT, STRING, EOF);
    }

    @Test
    public void listLiteralTokenization() {
        assertTokensAre("[1, 2, 3]", LEFT_BRACKET, INTEGER, COMMA, INTEGER, COMMA, INTEGER, RIGHT_BRACKET, EOF);
    }

    @Test
    public void linesAreCorrect() {
        final List<Token> tokenList = getTokensAsList("a\n b\n  c");
        assertEquals(1, tokenList.get(0).getLine());
        assertEquals(2, tokenList.get(1).getLine());
        assertEquals(3, tokenList.get(2).getLine());
    }

    @Test
    public void lineOffsetsAreCorrect() {
        final List<Token> tokenList = getTokensAsList("a\n b\n  c");
        assertEquals(0, tokenList.get(0).getLineOffset());
        assertEquals(1, tokenList.get(1).getLineOffset());
        assertEquals(2, tokenList.get(2).getLineOffset());
    }

    @Test
    public void startsAreCorrect() {
        final List<Token> tokenList = getTokensAsList("a\n b\n  c");
        assertEquals(0, tokenList.get(0).getStart());
        assertEquals(3, tokenList.get(1).getStart());
        assertEquals(7, tokenList.get(2).getStart());
    }

    @Test
    public void endsAreCorrect() {
        List<Token> tokenList = getTokensAsList("a\n b\n  c");
        assertEquals(1, tokenList.get(0).getEnd());
        assertEquals(4, tokenList.get(1).getEnd());
        assertEquals(8, tokenList.get(2).getEnd());

        tokenList = getTokensAsList("ayy\n bee\n  cee");
        assertEquals(3, tokenList.get(0).getEnd());
        assertEquals(8, tokenList.get(1).getEnd());
        assertEquals(14, tokenList.get(2).getEnd());
    }

    @Test
    public void varStatement(){
        assertTokensAre("var x = 10",
                VAR, IDENTIFIER, EQUAL, INTEGER, EOF);
    }

    //=================================================================
    //  Helpers
    //=================================================================

    private void assertTokensAre(String src, TokenType... expected) {
        TokenList tokens = getTokenList(src);
        assertEquals(Arrays.asList(expected), tokens.stream().map(Token::getType).collect(Collectors.toList()));
    }

    private TokenList getTokenList(String src) {
        CatScriptTokenizer tokenizer = new CatScriptTokenizer(src);
        return tokenizer.getTokens();
    }

    private List<Token> getTokensAsList(String src) {
        CatScriptTokenizer tokenizer = new CatScriptTokenizer(src);
        return tokenizer.getTokens().stream().collect(Collectors.toList());
    }

    private void assertTokensAre(String src, String... expected) {
        TokenList tokens = getTokenList(src);
        assertEquals(Arrays.asList(expected), tokens.stream().map(Token::getStringValue).collect(Collectors.toList()));
    }

}
