package edu.montana.csci.csci468.parser;

import edu.montana.csci.csci468.parser.expressions.*;
import edu.montana.csci.csci468.parser.statements.*;
import edu.montana.csci.csci468.tokenizer.CatScriptTokenizer;
import edu.montana.csci.csci468.tokenizer.Token;
import edu.montana.csci.csci468.tokenizer.TokenList;
import edu.montana.csci.csci468.tokenizer.TokenType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static edu.montana.csci.csci468.tokenizer.TokenType.*;

public class CatScriptParser {

    private TokenList tokens;
    private FunctionDefinitionStatement currentFunctionDefinition;

    public CatScriptProgram parse(String source) {
        tokens = new CatScriptTokenizer(source).getTokens();

        // first parse an expression
        CatScriptProgram program = new CatScriptProgram();
        program.setStart(tokens.getCurrentToken());
        Expression expression = parseExpression();
        if (tokens.hasMoreTokens()) {
            tokens.reset();
            while (tokens.hasMoreTokens()) {
                program.addStatement(parseProgramStatement());
            }
        } else {
            program.setExpression(expression);
        }

        program.setEnd(tokens.getCurrentToken());
        return program;
    }

    public CatScriptProgram parseAsExpression(String source) {
        tokens = new CatScriptTokenizer(source).getTokens();
        CatScriptProgram program = new CatScriptProgram();
        program.setStart(tokens.getCurrentToken());
        Expression expression = parseExpression();
        program.setExpression(expression);
        program.setEnd(tokens.getCurrentToken());
        return program;
    }

    //============================================================
    //  Statements
    //============================================================
    private Statement parseCatscriptProgram(){
//        while(tokens.hasMoreTokens()){
//
//            return parseProgramStatement();
//        }
//        Statement progStmt = parseProgramStatement();
        return null;
    }

    private Statement parseProgramStatement() {
        Statement printStmt = parsePrintStatement();
        Statement varStmt = parseVariableStatement();
        Statement forStmt = parseForStatement();
        Statement assStmt = parseAssignmentStatement();
        Statement ifStmt = parseIfStatement();
        Statement funcDec = parseFunctionDeclarationStatement();
        if (printStmt != null) {
            return printStmt;
        }
        else if (varStmt != null) {
            return varStmt;
        }
        else if (forStmt != null){
            return forStmt;
        }
        else if (assStmt != null){
            return assStmt;
        }
        else if (ifStmt != null){
            return ifStmt;
        }
        else if (tokens.match(FUNCTION)){
            return funcDec;
        }
        return null;
        //return new SyntaxErrorStatement(tokens.consumeToken());
    }

    private Statement parseStatement(){
        if(tokens.match(FOR)){
            return parseForStatement();
        }
        else if(tokens.match(IF)){
            return parseIfStatement();
        }
        else if(tokens.match(PRINT)){
            return parsePrintStatement();
        }
        else if(tokens.match(VAR)){
            return parseVariableStatement();
        }
        else if(tokens.match(IDENTIFIER)){
            return parseAssignmentStatement();
        }
        else if(tokens.match(FUNCTION)){
            //Token functionName = tokens.consumeToken();
            //return parseFunctionCallStatement(functionName);
        }

//        return new SyntaxErrorStatement(tokens.consumeToken());
        return null;
    }

    private Statement parseForStatement(){
        if(tokens.match(FOR)){
            ForStatement forStatement = new ForStatement();
            forStatement.setStart(tokens.consumeToken());
            require(LEFT_PAREN, forStatement);
            forStatement.setVariableName(tokens.consumeToken().getStringValue());
            require(IN, forStatement);
            forStatement.setExpression(parseExpression());
            require(RIGHT_PAREN, forStatement);
            require(LEFT_BRACE, forStatement);
            List<Statement> statements = new LinkedList<>();
            while(!tokens.match(RIGHT_BRACE)){
                statements.add(parseStatement());
                if(tokens.match(EOF) || tokens.match(RIGHT_BRACE)){
                    break;
                }
            }
            forStatement.setBody(statements);
            //parseProgramStatement();
            // TO DO statements
            forStatement.setEnd(require(RIGHT_BRACE, forStatement));
            return forStatement;
        }
        return null;
        //return new SyntaxErrorStatement(tokens.consumeToken());
    }

    private Statement parseIfStatement(){
        if(tokens.match(IF)){
            IfStatement ifStatement = new IfStatement();
            ifStatement.setStart(tokens.consumeToken());
            require(LEFT_PAREN, ifStatement);
            ifStatement.setExpression(parseExpression());
            require(RIGHT_PAREN, ifStatement);
            require(LEFT_BRACE, ifStatement);
            //ifStatement.setTrueStatements();
            List<Statement> statements = new LinkedList<>();
            while(!tokens.match(RIGHT_BRACE)){
                statements.add(parseStatement());
                if(tokens.match(EOF)){
                    break;
                }
            }
            ifStatement.setTrueStatements(statements);
            //parseProgramStatement();
            require(RIGHT_BRACE, ifStatement);
            if(tokens.match(ELSE)){
                tokens.consumeToken();
                require(LEFT_BRACE, ifStatement);
                if(tokens.match(EOF)){
                    ifStatement.addError(ErrorType.UNTERMINATED_ARG_LIST);
                    return ifStatement;
                }
                if(tokens.match(IF)){
                    parseIfStatement();
                }
                else {
                    if(tokens.match(EOF)){
                        ifStatement.addError(ErrorType.UNTERMINATED_ARG_LIST);
                    }
                    List<Statement> elseStatements = new LinkedList<>();
                    while(!tokens.match(RIGHT_BRACE)){
                        statements.add(parseStatement());
                        if(tokens.match(EOF)){
                            break;
                        }
                    }
                    ifStatement.setElseStatements(elseStatements);
                    //parseProgramStatement();
                    require(RIGHT_BRACE, ifStatement);

                }
            }
            require(RIGHT_BRACE, ifStatement);
            return ifStatement;
        }

       // return new SyntaxErrorStatement(tokens.consumeToken());
        return null;
    }

    private Statement parsePrintStatement() {
        if (tokens.match(PRINT)) {

            PrintStatement printStatement = new PrintStatement();
            printStatement.setStart(tokens.consumeToken());

            require(LEFT_PAREN, printStatement);
            printStatement.setExpression(parseExpression());
            printStatement.setEnd(require(RIGHT_PAREN, printStatement));

            return printStatement;
        }
        return null;
        //return new SyntaxErrorStatement(tokens.consumeToken());
    }

    private Statement parseVariableStatement(){
        if(tokens.match(VAR)){
            VariableStatement variableStatement = new VariableStatement();
            variableStatement.setStart(tokens.consumeToken());
            String identifierString = tokens.consumeToken().getStringValue();

            // to do optional : and type_expression
            if(tokens.match(COLON)){
                tokens.consumeToken();
                String explicitIdentifierString = tokens.consumeToken().getStringValue();
                CatscriptType variableType = parseTypeExpression(explicitIdentifierString);
                variableStatement.setExplicitType(variableType);

            }

            require(EQUAL,variableStatement);
            variableStatement.setVariableName(identifierString);
            Expression endExp = parseExpression();
            variableStatement.setExpression(endExp);
            variableStatement.setEnd(endExp.getEnd());

            return variableStatement;
        }
        return null;
        //return new SyntaxErrorStatement(tokens.consumeToken());
    }

    private Expression parseFunctionCallStatement(Token functionName){
        return parseFunctionCall(functionName);
    }

    private Statement parseAssignmentStatement(){
        if(tokens.match(IDENTIFIER)){
            AssignmentStatement assignmentStatement = new AssignmentStatement();
            assignmentStatement.setStart(tokens.getCurrentToken());
            String identifierString = tokens.consumeToken().getStringValue();
            assignmentStatement.setVariableName(identifierString);
            require(EQUAL, assignmentStatement);
            Expression endExp = parseExpression();
            assignmentStatement.setExpression(endExp);
            assignmentStatement.setEnd(endExp.getEnd());
            return assignmentStatement;
        }
        return null;
        //return new SyntaxErrorStatement(tokens.consumeToken());
    }

    private CatscriptType parseTypeExpression(String explicitIdentifierString){
        //explicitIdentifierString = tokens.consumeToken().getStringValue();
        if(explicitIdentifierString.equals("int")) {
            return  CatscriptType.INT;
            //stringValue.setExplicitType(CatscriptType.INT);
        }
        else if(explicitIdentifierString.equals("bool")) {
            return CatscriptType.BOOLEAN;
            //stringValue.setExplicitType(CatscriptType.BOOLEAN);
        }
        else if(explicitIdentifierString.equals("string")) {
            return CatscriptType.STRING;
            //stringValue.setExplicitType(CatscriptType.STRING);
        }
        else if(explicitIdentifierString.equals("object")) {
            return CatscriptType.OBJECT;
            //stringValue.setExplicitType(CatscriptType.OBJECT);
        }
        else if(explicitIdentifierString.equals("list")) {
            tokens.consumeToken();
            String listType = tokens.consumeToken().getStringValue();
            if(listType.equals("int")) {
                tokens.consumeToken();
                return CatscriptType.getListType(CatscriptType.INT);
            }
            //stringValue.setExplicitType(CatscriptType.INT);
        }

        return null;
    }

    private void parseParameter(){

    }

    private void parseParameterList(){

    }

    private Statement parseFunctionDeclarationStatement(){
        if(tokens.match(FUNCTION)){
            tokens.consumeToken();


        }
        return null;
    }

    private Statement parseFunctionBodyStatement(){
        if(tokens.match(RETURN)){
            return parseReturnStatement();
        }
        else{
            return parseStatement();
        }
    }

    private Statement parseParameterListStatement(){
        return null;
    }

    private Statement parseParameterStatement(){
        if(tokens.match(IDENTIFIER)){

        }
        return null;
    }

    private Statement parseReturnStatement(){
        if (tokens.match(RETURN)){
            ReturnStatement returnStatement = new ReturnStatement();
            returnStatement.setExpression(parseExpression());
            return returnStatement;
        }
        return null;
        //return new SyntaxErrorStatement(tokens.consumeToken());

    }

    //============================================================
    //  Expressions
    //============================================================

    private Expression parseExpression() {
        return  parseEqualityExpression();
    }

    private Expression parseAdditiveExpression() {
        Expression expression = parseFactorExpression();
        while (tokens.match(PLUS, MINUS)) {
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseFactorExpression();
            AdditiveExpression additiveExpression = new AdditiveExpression(operator, expression, rightHandSide);
            additiveExpression.setStart(expression.getStart());
            additiveExpression.setEnd(rightHandSide.getEnd());
            expression = additiveExpression;
        }
        return expression;
    }
    private Expression parseFactorExpression() {
        Expression expression = parseUnaryExpression();
        while (tokens.match(STAR, SLASH)){
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseUnaryExpression();
            FactorExpression factorExpression = new FactorExpression(operator, expression, rightHandSide);
            factorExpression.setStart(expression.getStart());
            factorExpression.setEnd(rightHandSide.getEnd());
            expression = factorExpression;
        }
        return expression;
    }
    private Expression parseComparisonExpression(){
        Expression expression = parseAdditiveExpression();
        while (tokens.match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)){
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseAdditiveExpression();
            ComparisonExpression comparisonExpression = new ComparisonExpression(operator, expression, rightHandSide);
            comparisonExpression.setStart(expression.getStart());
            comparisonExpression.setEnd(rightHandSide.getEnd());
            expression = comparisonExpression;

        }
        return  expression;
    }
    private Expression parseEqualityExpression(){
        Expression expression = parseComparisonExpression();
        while (tokens.match(BANG_EQUAL, EQUAL_EQUAL)){
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseComparisonExpression();
            EqualityExpression equalityExpression = new EqualityExpression(operator, expression,rightHandSide);
            equalityExpression.setStart(expression.getStart());
            equalityExpression.setEnd(rightHandSide.getEnd());
            expression = equalityExpression;
        }
        return expression;
    }

    private Expression parseUnaryExpression() {
        if (tokens.match(MINUS, NOT)) {
            Token token = tokens.consumeToken();
            Expression rhs = parseUnaryExpression();
            UnaryExpression unaryExpression = new UnaryExpression(token, rhs);
            unaryExpression.setStart(token);
            unaryExpression.setEnd(rhs.getEnd());
            return unaryExpression;
        } else {
            return parsePrimaryExpression();
        }
    }

    private Expression parsePrimaryExpression() {
        if (tokens.match(INTEGER)) {
            Token integerToken = tokens.consumeToken();
            IntegerLiteralExpression integerExpression = new IntegerLiteralExpression(integerToken.getStringValue());
            integerExpression.setToken(integerToken);
            return integerExpression;
        }
        else if(tokens.match(IDENTIFIER)) {
            Token identifierToken = tokens.consumeToken();

            if(tokens.match(LEFT_PAREN)) {
                return parseFunctionCall(identifierToken);
            }

            IdentifierExpression identifierExpression = new IdentifierExpression(identifierToken.getStringValue());
            identifierExpression.setToken(identifierToken);
            return identifierExpression;
        }
        else if(tokens.match(LEFT_BRACKET)){
            List<Expression> listExpressions = new ArrayList<>();
            tokens.consumeToken();
            if(tokens.match(RIGHT_BRACKET)){
                tokens.consumeToken();
                ListLiteralExpression listLiteralExpression = new ListLiteralExpression(listExpressions);
                return listLiteralExpression;
            }
            else {
                Expression startExpression = parseExpression();
                listExpressions.add(startExpression);
                while (tokens.match(COMMA)) {
                    tokens.consumeToken();
                    Expression elementExpressions = parseExpression();
                    listExpressions.add(elementExpressions);

                }
                boolean unterminated = false;
                if(!tokens.match(RIGHT_BRACKET)){
                    // throw an unterminated list error
                    unterminated = true;
                }
                if(tokens.match(RIGHT_BRACKET)) {
                    tokens.consumeToken();
                }
                ListLiteralExpression listLiteralExpression = new ListLiteralExpression(listExpressions);
                if(unterminated){
                    listLiteralExpression.addError(ErrorType.UNTERMINATED_LIST);
                }
                return listLiteralExpression;
            }
        }
        else if(tokens.match(STRING)) {
            Token stringToken = tokens.consumeToken();
            StringLiteralExpression stringLiteralExpression = new StringLiteralExpression(stringToken.getStringValue());
            stringLiteralExpression.setToken(stringToken);
            return stringLiteralExpression;
        }

        else if(tokens.match(TRUE)) {
            Token trueToken = tokens.consumeToken();
            BooleanLiteralExpression trueExpression = new BooleanLiteralExpression(true);
            trueExpression.setToken(trueToken);
            return trueExpression;
        }
        else if(tokens.match(FALSE)) {
            Token falseToken = tokens.consumeToken();
            BooleanLiteralExpression falseExpression = new BooleanLiteralExpression(false);
            falseExpression.setToken(falseToken);
            return falseExpression;
        }
        else if(tokens.match(NULL)) {
            Token nullToken = tokens.consumeToken();
            NullLiteralExpression nullExpression = new NullLiteralExpression();
            nullExpression.setToken(nullToken);
            return nullExpression;
        }

        else if(tokens.match(LEFT_PAREN)){
            Token startToken = tokens.consumeToken();
            Expression expression = parseExpression();
            boolean endParen = tokens.match(RIGHT_PAREN);
            tokens.consumeToken();
            ParenthesizedExpression parenthesizedExpression = new ParenthesizedExpression(expression);
            return parenthesizedExpression;
        }

            else {
            SyntaxErrorExpression syntaxErrorExpression = new SyntaxErrorExpression(tokens.consumeToken());
            return syntaxErrorExpression;
        }
    }
    private Expression parseFunctionCall(Token functionName){
        List<Expression> funcArgs = new ArrayList<>();
        Token firstParen = tokens.consumeToken();
        if (tokens.match(RIGHT_PAREN)) {
            FunctionCallExpression functionCallExpression = new FunctionCallExpression(functionName.getStringValue(), funcArgs);
            return functionCallExpression;

        }

        else {
            Expression firstArg = parseExpression();
            funcArgs.add(firstArg);
            while (tokens.match(COMMA)) {
                tokens.consumeToken();
                Expression argument = parseExpression();
                funcArgs.add(argument);
            }
        }
        boolean unterminated = false;
        if (!tokens.match(RIGHT_PAREN)) {
            // throw an unterminated arg list error
            unterminated = true;

        }
        FunctionCallExpression functionCallExpression = new FunctionCallExpression(functionName.getStringValue(), funcArgs);
        if(unterminated){
            functionCallExpression.addError(ErrorType.UNTERMINATED_ARG_LIST);
        }
        return functionCallExpression;



    }

    //============================================================
    //  Parse Helpers
    //============================================================
    private Token require(TokenType type, ParseElement elt) {
        return require(type, elt, ErrorType.UNEXPECTED_TOKEN);
    }

    private Token require(TokenType type, ParseElement elt, ErrorType msg) {
        if(tokens.match(type)){
            return tokens.consumeToken();
        } else {
            elt.addError(msg, tokens.getCurrentToken());
            return tokens.getCurrentToken();
        }
    }

}
