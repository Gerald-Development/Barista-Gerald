package main.java.de.voidtech.gerald.util;

import javax.management.RuntimeErrorException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

public class ArithmeticUtils {

    private static final long MILLIS_TIME_OUT = 5000;
    private static final String EXPRESSION_REGEX = "[0-9+\\-*/^()\\s.]+";
    private enum TokenType { VALUE, OPERATOR, EXPRESSION, TIMEOUT }

    private static class ArithmeticToken {

        private final TokenType type;
        private double valueMaybe;
        private String expressionMaybe;
        private char operatorMaybe;

        private static class ArithmeticOperator {
            private final char operator;
            private final BinaryOperator<Double> operation;
            private final Optional<Character> inverse;
            private final Optional<UnaryOperator<Double>> inverseAction;

            public ArithmeticOperator(char operator, BinaryOperator<Double> operation) {
                this.operator = operator;
                this.operation = operation;
                inverse = Optional.empty();
                this.inverseAction = Optional.empty();
            }

            public ArithmeticOperator(char operator, BinaryOperator<Double> operation, char inverse, UnaryOperator<Double> inverseAction) {
                this.operator = operator;
                this.operation = operation;
                this.inverse = Optional.of(inverse);
                this.inverseAction = Optional.of(inverseAction);
            }

            private static void validateIndice(int index, List<ArithmeticToken> tokens, char op) throws ArithmeticException{
                if (index == tokens.size() - 1) throw new ArithmeticException(
                        String.format("Expression ended with a '%c' unexpectedly", op));
                if (index == 0) throw new ArithmeticException(
                        String.format("Expression cannot start with '%c'", op));
                if (tokens.get(index + 1).type != TokenType.VALUE || tokens.get(index - 1).type != TokenType.VALUE)
                    throw new ArithmeticException(String.format("'%c' not used like an operator", op));
            }

            public List<ArithmeticToken> apply(List<ArithmeticToken> tokens) throws ArithmeticException{
                List<ArithmeticToken> newTokens = ArithmeticToken.copyTokenList(tokens);
                if(inverse.isPresent()){
                    if(!inverseAction.isPresent()) throw new Error(String.format("Uh oh somebody fucked with the code related to %s where they weren't supposed to",this.getClass().getCanonicalName()));
                    int[] inverseIndices = IntStream.range(0, newTokens.size())
                            .filter(i -> newTokens.get(i).type == TokenType.OPERATOR && newTokens.get(i).operatorMaybe == this.inverse.get())
                            .toArray();
                    for(int i : inverseIndices){
                        validateIndice(i, newTokens, this.inverse.get());

                        newTokens.set(i+1, new ArithmeticToken(this.inverseAction.get().apply(newTokens.get(i+1).valueMaybe)));
                        newTokens.set(i, new ArithmeticToken(this.operator));
                    }
                }

                int[] operatorIndices = IntStream.range(0, newTokens.size())
                        .filter(i -> newTokens.get(i).type == TokenType.OPERATOR && newTokens.get(i).operatorMaybe == this.operator)
                        .toArray();

                for (int n = 0; n < operatorIndices.length;n++) {
                    int i = operatorIndices[n];
                    validateIndice(i, newTokens, this.operator);

                    double second = newTokens.remove(i + 1).valueMaybe;
                    double first = newTokens.remove(i - 1).valueMaybe;
                    newTokens.set(i - 1, new ArithmeticToken(this.operation.apply(first, second)));
                    operatorIndices = Arrays.stream(operatorIndices).map((val)->val-2).toArray();
                }
                return newTokens;
            }
        }

        public ArithmeticToken() {
            this.type = TokenType.TIMEOUT;
        }

        public ArithmeticToken(double value) {
            this.type = TokenType.VALUE;
            this.valueMaybe = value;
        }

        public ArithmeticToken(String expression) {
            this.type = TokenType.EXPRESSION;
            this.expressionMaybe = expression;
        }

        public ArithmeticToken(char operator) {
            this.type = TokenType.OPERATOR;
            this.operatorMaybe = operator;
        }

        public String toString() {
            switch(this.type) {
                case OPERATOR:
                    return String.format("OP(%c)", this.operatorMaybe);
                case VALUE:
                    return String.format("VAL(%f)", this.valueMaybe);
                case EXPRESSION:
                    return String.format("EXPR(%s)", this.expressionMaybe);
                case TIMEOUT:
                    return "TIMEOUT";
                default:
                    return "BAD";
            }
        }

        //should never return an expression token
        public ArithmeticToken evalToken(long creationTime) throws RuntimeErrorException, ArithmeticException {

            if (System.currentTimeMillis() - creationTime > MILLIS_TIME_OUT) return new ArithmeticToken();
            if (this.type != TokenType.EXPRESSION) return this;

            List<ArithmeticToken> tokens;

            try {
                tokens = parseTokens(tokenize(this.expressionMaybe, creationTime));
            } catch(TimeoutException e) {
                return new ArithmeticToken();
            }

            if (tokens.size() == 1 && tokens.get(0).type == TokenType.VALUE) return tokens.get(0);
            else {
                throw new RuntimeErrorException(new Error("Unknown tokenization error"));
            }
        }
        public static ArithmeticToken constructDecimal(long sum, long decimal, long leadingZeroes){
            return new ArithmeticToken(sum +
                    ((double)decimal) / Math.pow(10, (decimal != 1 ? Math.ceil(Math.log(decimal) / Math.log(10)):1)+leadingZeroes));
        }
        public static List<ArithmeticToken> tokenize(String expression, long creation) throws TimeoutException, ArithmeticException {
            List<ArithmeticToken> children = new LinkedList<>();
            boolean leadZeroesOngoing = true;
            boolean implicitMul = false;
            for (int parsePoint = 0, parenLevel = 0, tempSum = -1, tempDecimal = -1,
                 prevParenOpen=-1, leadingZeroes = 0; parsePoint<expression.length(); parsePoint++) {

                char parsedChar = expression.charAt(parsePoint);
                if (parsedChar == ')') {
                    parenLevel--;
                    if (parenLevel == 0) {
                        if(implicitMul) children.add(new ArithmeticToken('*'));
                        children.add(new ArithmeticToken(expression.substring(prevParenOpen + 1, parsePoint)).evalToken(creation));
                        prevParenOpen = -1;
                    } else if(parenLevel < 0) throw new ArithmeticException("You cannot have a close paren before its opening paren");
                    implicitMul = false;
                } else if(parenLevel == 0) {

                    switch(parsedChar) {
                        case '(':
                            prevParenOpen = parsePoint;
                            if (tempSum > -1) {
                                implicitMul = true;
                                if (tempDecimal == -1) children.add(new ArithmeticToken(tempSum));
                                else children.add(new ArithmeticToken(tempSum + ((double)tempDecimal) /
                                        Math.pow(10, Math.ceil(Math.log(tempDecimal) / Math.log(10)))));
                                tempSum = -1;
                                tempDecimal = -1;
                                leadZeroesOngoing = true;
                            }
                            parenLevel++;
                            break;

                        case '^':
                        case '/':
                        case '*':
                        case '+':
                        case '-':
                            if (tempSum > -1 || tempDecimal > -1) {

                                if (tempDecimal == -1) children.add(new ArithmeticToken(tempSum));
                                else children.add(constructDecimal(tempSum,tempDecimal,leadingZeroes));

                                tempSum = -1;
                                tempDecimal = -1;
                                leadingZeroes = 0;
                                leadZeroesOngoing = true;
                            }
                            children.add(new ArithmeticToken(expression.charAt(parsePoint)));
                            break;

                        case '.':
                            if (tempDecimal == -1 && parsePoint != expression.length() - 1) tempDecimal = 0;
                            else throw new ArithmeticException("Invalid usage of a decimal point");
                            break;
                        default:
                            if (tempSum == -1) tempSum++;
                            if (tempDecimal == -1) {
                                tempSum *= 10;
                                //turn the parsed char into it's equivalent number via ascii magic
                                tempSum += parsedChar - '0';
                                if (parsePoint == expression.length() - 1) children.add(new ArithmeticToken(tempSum));
                            }
                            else {
                                tempDecimal *= 10;
                                tempDecimal += parsedChar-'0';
                                if (parsedChar-'0' == 0 && leadZeroesOngoing)leadingZeroes++;
                                else leadZeroesOngoing = false;
                                if (parsePoint == expression.length()-1) children.add(constructDecimal(tempSum,tempDecimal,leadingZeroes));
                            }
                    }
                    if (children.size() > 0 && children.get(children.size() - 1).type == TokenType.TIMEOUT)
                        throw new TimeoutException("Child timed out");
                }
                else if (parsedChar == '(') parenLevel++;
            }
            return children;
        }

        public static List<ArithmeticToken> parseTokens(List<ArithmeticToken> tokens){
            for (int i = tokens.size() - 1; i >= 0; i--) {
                if (tokens.get(i).type == TokenType.OPERATOR && tokens.get(i).operatorMaybe == '-'
                        && (tokens.get(i + 1).type == TokenType.VALUE && (i == 0 || tokens.get(i - 1).type == TokenType.OPERATOR))) {
                    tokens.remove(i);
                    tokens.get(i).valueMaybe *= -1;
                }
            }

            ArithmeticOperator exponentOperator = new ArithmeticOperator('^', Math::pow);
            ArithmeticOperator multiplicationOperator = new ArithmeticOperator('*', (n1, n2) -> n1 * n2,'/',(val) -> 1/val);
            ArithmeticOperator additionOperator = new ArithmeticOperator('+', Double::sum,'-',(val)-> -val);
            tokens = exponentOperator.apply(tokens);
            tokens = multiplicationOperator.apply(tokens);
            tokens = additionOperator.apply(tokens);

            return tokens;
        }

        public static List<ArithmeticToken> copyTokenList(List<ArithmeticToken> tokens) {
            List<ArithmeticToken> newTokens = new ArrayList<>(tokens.size());

            for (ArithmeticToken token : tokens) {
                switch(token.type){
                    case OPERATOR:
                        newTokens.add(new ArithmeticToken(token.operatorMaybe));
                        break;
                    case VALUE:
                        newTokens.add(new ArithmeticToken(token.valueMaybe));
                        break;
                    case EXPRESSION:
                        newTokens.add(new ArithmeticToken(token.expressionMaybe));
                        break;
                    case TIMEOUT:
                        newTokens.add(new ArithmeticToken());
                        break;
                }
            }
            return newTokens;
        }
    }

    public static double evalExpression(String expression, long creationTime) throws ArithmeticException {

        if (!isValidExpression(expression))
            throw new ArithmeticException("Invalid char(s) in expression. Must only contain numbers, + - * / ^ ( )");

        expression = expression.replaceAll("\\s","");

        int openParenCount = 0;
        int closeParenCount = 0;
        for (int i = 0; i < expression.length(); i++) {
            if (expression.charAt(i) == '(') openParenCount++;
            else if (expression.charAt(i) == ')') closeParenCount++;
        }
        if (openParenCount != closeParenCount)
            throw new ArithmeticException("Parentheses must match up in expression");

        return new ArithmeticToken(expression).evalToken(creationTime).valueMaybe;
    }

    public static boolean isValidExpression(String expression) {
        return expression.matches(EXPRESSION_REGEX);
    }

}
