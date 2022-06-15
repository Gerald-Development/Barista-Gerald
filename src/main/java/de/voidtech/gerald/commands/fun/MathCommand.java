package main.java.de.voidtech.gerald.commands.fun;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.stream.IntStream;

import javax.management.RuntimeErrorException;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.service.LogService;
import main.java.de.voidtech.gerald.util.GeraldLogger;

@Command
public class MathCommand extends AbstractCommand {
	
    private static final long MILLIS_TIME_OUT = 5000;
    private static final String EXPRESSION_REGEX = "[0-9+\\-*/^()\\s.]+";
    private static final GeraldLogger LOGGER = LogService.GetLogger(MathCommand.class.getSimpleName());
    
    private enum TokenType {VALUE,OPERATOR,EXPRESSION, TIMEOUT}

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
            private static void validateIndice(int indice, List<ArithmeticToken> tokens,char op) throws ArithmeticException{
                if (indice == tokens.size() - 1) throw new ArithmeticException(
                        String.format("Expression ended with a '%c' unexpectedly", op));
                if (indice == 0) throw new ArithmeticException(
                        String.format("Expression cannot start with '%c'", op));
                if (tokens.get(indice + 1).type != TokenType.VALUE || tokens.get(indice - 1).type != TokenType.VALUE)
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
                        operatorIndices = (int[]) Arrays.stream(operatorIndices).map((val)->val-2).toArray();
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
                LOGGER.log(Level.SEVERE, String.format("Error during CommandExecution: '%s' turned into '%s' rather than resolving to a single value",
                		this.expressionMaybe, Arrays.deepToString(tokens.toArray())));
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

    private double evalExpression(String expression, long creationTime) throws ArithmeticException {
    	
        if (!expression.matches(EXPRESSION_REGEX))
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
    
    @Override
    public void executeInternal(CommandContext context, List<String> args) {
        String expression = String.join(" ",args);
        long creation = System.currentTimeMillis();
        try {
            double result = evalExpression(expression, creation);
            context.reply(String.format("The result of your expression `%s` is `%f`", expression, result));
        } catch (ArithmeticException e){
            context.reply(String.format("Arithmetic Error: %s", e.getMessage()));
        }
        // give feedback to the user in case of an unexpected error.
        // Yes I know catching Exception is bad practice but it's to future proofing against more exceptions being thrown
        catch (Exception e){
            context.reply(String.format("Internal Error `%s`, you should copy the command you sent and error you got and tell us about it at https://discord.gg/RwftadXcCv", e.getMessage()));
        }

    }

    @Override
    public String getDescription() {
        return "When you need to do a bit of simple math but don't want to do it yourself";
    }

    @Override
    public String getUsage() {
        return "math [expression]";
    }

    @Override
    public String getName() {
        return "math";
    }

    @Override
    public CommandCategory getCommandCategory() {
        return CommandCategory.FUN;
    }

    @Override
    public boolean isDMCapable() {
        return true;
    }

    @Override
    public boolean requiresArguments() {
        return true;
    }

    @Override
    public String[] getCommandAliases() {
        return new String[]{"m", "π", "eval", "calc"};
    }

    @Override
    public boolean canBeDisabled() {
        return true;
    }
    
	@Override
	public boolean isSlashCompatible() {
		return true;
	}
}
