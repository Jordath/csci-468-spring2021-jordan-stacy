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
        Statement statement = parseStatement();
        if(statement != null){
            return statement;
        }
        statement = parseFunctionDeclarationStatement();
        if(statement != null){
            return statement;
        }
        return null;
        //return new SyntaxErrorStatement(tokens.consumeToken());
    }

    private Statement parseStatement(){
        Statement printStmt = parsePrintStatement();
        if (printStmt != null) {
            return printStmt;
        }
        Statement varStmt = parseVariableStatement();
        if (varStmt != null) {
            return varStmt;
        }
        Statement forStmt = parseForStatement();
        if (forStmt != null){
            return forStmt;
        }
        Statement assStmt = parseAssignmentStatement();
        if (assStmt != null){
            return assStmt;
        }
        Statement ifStmt = parseIfStatement();

        if (ifStmt != null){
            return ifStmt;
        }
        //Statement funcDec = parseFunctionDeclarationStatement();
        Statement retStmt = parseReturnStatement();
        if (retStmt != null){
            return retStmt;
        }





//        else if (tokens.match(FUNCTION)){
//            return funcDec;
//        }


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
            Expression parsedBool = parseExpression();
            ifStatement.setExpression(parsedBool);
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
                        elseStatements.add(parseStatement());
                        if(tokens.match(EOF)){
                            break;
                        }
                    }
                    ifStatement.setElseStatements(elseStatements);
                    //parseProgramStatement();
                    require(RIGHT_BRACE, ifStatement);

                }
            }
            //require(RIGHT_BRACE, ifStatement);
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

    private Statement parseAssignmentStatement(){
        if(tokens.match(IDENTIFIER)){
            Token identifier = tokens.getCurrentToken();
            String identifierString = tokens.consumeToken().getStringValue();

            if(tokens.match(LEFT_PAREN)){

                List<Expression> funcArgs = new ArrayList<>();
                Token firstParen = tokens.consumeToken();
                if (tokens.match(RIGHT_PAREN)) {
                    FunctionCallExpression functionCallExpression = new FunctionCallExpression(identifierString, funcArgs);
                    FunctionCallStatement functionCallStatement = new FunctionCallStatement(functionCallExpression);
                    tokens.consumeToken();
                    return functionCallStatement;

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
                FunctionCallExpression functionCallExpression = new FunctionCallExpression(identifierString,funcArgs);
                FunctionCallStatement functionCallStatement = new FunctionCallStatement(functionCallExpression);
                if(unterminated){
                    functionCallExpression.addError(ErrorType.UNTERMINATED_ARG_LIST);
                }
                tokens.consumeToken();
                return functionCallStatement;


            }
            else if(tokens.match(EQUAL)) {

                AssignmentStatement assignmentStatement = new AssignmentStatement();
                assignmentStatement.setStart(tokens.getCurrentToken());
                assignmentStatement.setVariableName(identifierString);
                require(EQUAL, assignmentStatement);
                Expression endExp = parseExpression();
                assignmentStatement.setExpression(endExp);
                assignmentStatement.setEnd(endExp.getEnd());
                return assignmentStatement;
            }
        }
        return null;
        //return new SyntaxErrorStatement(tokens.consumeToken());
    }

    private CatscriptType parseTypeExpression(String explicitIdentifierString){
        Token currentToken = tokens.getCurrentToken();
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
    private TypeLiteral parseTypeLiteral(){
        Token currentToken = tokens.getCurrentToken();
        if(currentToken.getType() == IDENTIFIER){
            String stringVal = currentToken.getStringValue();
            TypeLiteral typeLiteral = new TypeLiteral();
            if(stringVal.equals("int")){
                typeLiteral.setType(CatscriptType.INT);
                return typeLiteral;
            }
            else if(stringVal.equals("string")){
                typeLiteral.setType(CatscriptType.STRING);
                return typeLiteral;
            }
            else if(stringVal.equals("bool")){
                typeLiteral.setType(CatscriptType.BOOLEAN);
                return typeLiteral;
            }
            else if(stringVal.equals("object")){
                typeLiteral.setType(CatscriptType.OBJECT);
                return typeLiteral;
            }
            else if(stringVal.equals("list")){
                tokens.consumeToken();
                if(tokens.match(LEFT_BRACE)){
                    typeLiteral.setType(CatscriptType.getListType(CatscriptType.OBJECT));
                    return typeLiteral;
                }
                require(LESS, typeLiteral);
                //tokens.consumeToken();
                String listType = tokens.consumeToken().getStringValue();
                if(listType.equals("int")) {
                    require(GREATER, typeLiteral);
                    //tokens.consumeToken();
                    typeLiteral.setType(CatscriptType.getListType(CatscriptType.INT));
                    return typeLiteral;
                }
                if(listType.equals("string")) {
                    tokens.consumeToken();
                    typeLiteral.setType(CatscriptType.getListType(CatscriptType.INT));
                    return typeLiteral;
                }
            }

        }
        return null;
    }

    private Statement parseFunctionDeclarationStatement(){
        if(tokens.match(FUNCTION)){
            try {
                //FunctionDefinitionStatement functionDefinitionStatement = new FunctionDefinitionStatement();
                currentFunctionDefinition = new FunctionDefinitionStatement();
                Token start = tokens.consumeToken();
                currentFunctionDefinition.setStart(start);
                Token functionName = require(IDENTIFIER, currentFunctionDefinition);
                currentFunctionDefinition.setName(functionName.getStringValue());
                require(LEFT_PAREN, currentFunctionDefinition);
                // parameter_list
                while (!tokens.match(RIGHT_PAREN)) {
                    if (tokens.match(EOF)) {
                        break;
                    } else {
                        Token paramToken = tokens.consumeToken();
                        if (tokens.match(COLON)) {
                            tokens.consumeToken();
                            TypeLiteral paramType = parseTypeLiteral();
                            currentFunctionDefinition.addParameter(paramToken.getStringValue(), paramType);
                            tokens.consumeToken();
                        } else {
                            TypeLiteral objectType = new TypeLiteral();
                            objectType.setType(CatscriptType.OBJECT);
                            currentFunctionDefinition.addParameter(paramToken.getStringValue(), objectType);
                        }

                    }
                    if (!tokens.match(RIGHT_PAREN)) {
                        tokens.consumeToken();
                    }
                }
                require(RIGHT_PAREN, currentFunctionDefinition);
                if (tokens.match(COLON)) {
                    tokens.consumeToken();
                    TypeLiteral functionType = parseTypeLiteral();
                    currentFunctionDefinition.setType(functionType);
                    //klsdf nsdvkj
                    tokens.consumeToken();
                    if(!tokens.match(LEFT_BRACE)) {
                        tokens.consumeToken();
                    }
                } else {
                    TypeLiteral voidType = new TypeLiteral();
                    voidType.setType(CatscriptType.VOID);
                    currentFunctionDefinition.setType(voidType);
                }
                require(LEFT_BRACE, currentFunctionDefinition);
                List<Statement> functionBodyStatements = new LinkedList<>();
                while (!tokens.match(RIGHT_BRACE)) {
                    if (tokens.match(EOF)) {
                        break;
                    } else {
                        functionBodyStatements.add(parseFunctionBodyStatement());
                    }
                }
                currentFunctionDefinition.setBody(functionBodyStatements);

                if (tokens.match(EOF)) {
                    currentFunctionDefinition.addError(ErrorType.UNTERMINATED_ARG_LIST);
                }
                currentFunctionDefinition.setEnd(require(RIGHT_BRACE, currentFunctionDefinition));
                return currentFunctionDefinition;
            } finally {
                currentFunctionDefinition = null;
            }
        }
        return null;
    }

    private Statement parseFunctionBodyStatement(){
            return parseStatement();
    }

    private Statement parseReturnStatement(){
        if (tokens.match(RETURN)){
            ReturnStatement returnStatement = new ReturnStatement();
            returnStatement.setStart(tokens.consumeToken());
            returnStatement.setFunctionDefinition(currentFunctionDefinition);
            if(tokens.match(RIGHT_BRACE)){
                returnStatement.setEnd(tokens.getCurrentToken());
                return returnStatement;
            }
            else {
                returnStatement.setExpression(parseExpression());
                returnStatement.setEnd(tokens.getCurrentToken());
                return returnStatement;
            }
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
//            if(tokens.match(RIGHT_BRACE)){
//                tokens.consumeToken();
//            }
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
            tokens.consumeToken();
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
