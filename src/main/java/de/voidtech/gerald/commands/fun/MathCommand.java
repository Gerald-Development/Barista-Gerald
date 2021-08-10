package main.java.de.voidtech.gerald.commands.fun;

import java.util.*;
import java.util.function.ObjIntConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import net.dv8tion.jda.api.entities.Message;

@Command
public class MathCommand extends AbstractCommand {
    private static final long MILLIS_TIME_OUT = 5000;
    private static final Logger LOGGER = Logger.getLogger(MathCommand.class.getName());
    private enum TokenType{VALUE,OPERATOR,EXPRESSION, TIMEOUT};
    private class ArithmeticToken {
        private TokenType type;
        // check the type specified in TokenType before looking in Maybe values to avoid getting garbage unless you
        // know with high confidence which type it should be
        double valueMaybe;
        String expressionMaybe;
        char operatorMaybe;
        public TokenType getType(){
            return this.type;
        }
        public ArithmeticToken(){
            this.type = TokenType.TIMEOUT;
        }
        public ArithmeticToken(double value){
            this.type = TokenType.VALUE;
            this.valueMaybe = value;
        }
        public ArithmeticToken(String expression){
            this.type = TokenType.EXPRESSION;
            this.expressionMaybe = expression;
        }
        public ArithmeticToken(char operator){
            this.type = TokenType.OPERATOR;
            this.operatorMaybe = operator;
        }
        public String toString(){
            switch(this.type){
                case OPERATOR: return String.format("OP(%c)",this.operatorMaybe);
                case VALUE: return String.format("VAL(%f)",this.valueMaybe);
                case EXPRESSION: return String.format("EXPR(%s)",this.expressionMaybe);
                case TIMEOUT: return "TIME";
                default: return "BAD";
            }
        }
        //should never return an expression token
        public ArithmeticToken evalToken(long creation) throws Error, ArithmeticException{
            //LOGGER.log(Level.INFO, String.format("'%s' evaled",this.expressionMaybe));
            //change the condition in the if statement to be true if a timeout is reached

            if(System.currentTimeMillis()-creation > MILLIS_TIME_OUT) return new ArithmeticToken();
            if(this.type != TokenType.EXPRESSION) return this;
            List<ArithmeticToken> children = new LinkedList<>();
            for(int parsePoint = 0, parenLevel = 0, tempSum = -1,tempDecimal = -1,prevParenOpen=-1;parsePoint<this.expressionMaybe.length();parsePoint++){
                char parsedChar = this.expressionMaybe.charAt(parsePoint);
                if(parsedChar == ')'){
                    parenLevel--;
                    if(parenLevel==0){

                        children.add(new ArithmeticToken(this.expressionMaybe.substring(prevParenOpen+1,parsePoint)).evalToken(creation));
                        prevParenOpen = -1;
                    }
                }
                else if(parenLevel==0) {
                    switch(parsedChar){
                            case '(':
                                prevParenOpen = parsePoint;
                                if(tempSum > -1){
                                    if(tempDecimal == -1){
                                        children.add(new ArithmeticToken(tempSum));
                                    }
                                    else{
                                        children.add(new ArithmeticToken(tempSum+
                                                ((double)tempDecimal)/Math.pow(10,Math.ceil(Math.log(tempDecimal)/Math.log(10))))
                                                );
                                    }
                                    tempSum = -1;
                                    tempDecimal = -1;
                                }
                                parenLevel++;
                                break;

                            case '^':
                            case '/':
                            case '*':
                            case '+':
                            case '-':
                                if(tempSum > -1){
                                    if(tempDecimal == -1){
                                        children.add(new ArithmeticToken(tempSum));
                                    }
                                    else{
                                        children.add(new ArithmeticToken(tempSum+
                                                ((double)tempDecimal)/Math.pow(10,Math.ceil(Math.log(tempDecimal)/Math.log(10)))));
                                    }
                                    tempSum = -1;
                                    tempDecimal = -1;
                                }
                                children.add(new ArithmeticToken(this.expressionMaybe.charAt(parsePoint)));
                                break;

                            case '.':
                                if(tempDecimal == -1 && parsePoint != this.expressionMaybe.length()-1) tempDecimal = 0;
                                else throw new ArithmeticException("Invalid usage of a decimal point");
                                break;
                            default:
                                if(tempSum==-1) tempSum++;

                                if(tempDecimal==-1){
                                    tempSum*=10;
                                    tempSum+=((byte)parsedChar)-((byte)'0');
                                    if(parsePoint==this.expressionMaybe.length()-1){
                                        children.add(new ArithmeticToken(tempSum));
                                    }
                                }
                                else {
                                    tempDecimal*=10;
                                    tempDecimal+=((byte)parsedChar)-((byte)'0');
                                    if(parsePoint==this.expressionMaybe.length()-1){
                                        children.add(new ArithmeticToken(tempSum+
                                                ((double)tempDecimal)/Math.pow(10,Math.ceil(Math.log(tempDecimal)/Math.log(10)))));
                                    }
                                }
                    }
                    if(children.size()>0 && children.get(children.size()-1).type == TokenType.TIMEOUT) return new ArithmeticToken();
                }
                else if(parsedChar == '(') parenLevel++;

            }
            //I don't know why I can't reuse the children variable here but apparently that's illegal
            List<ArithmeticToken> tokens = new ArrayList<>(children);

            for(int i = tokens.size()-1;i>=0;i--){
                if(tokens.get(i).type == TokenType.OPERATOR && tokens.get(i).operatorMaybe == '-'
                        && (tokens.get(i+1).type == TokenType.VALUE && (i == 0 || tokens.get(i-1).type == TokenType.OPERATOR))){
                    tokens.remove(i);
                    tokens.get(i).valueMaybe *= -1;
                }
            }

            int[] expIndices = IntStream.range(0, tokens.size())
                    .filter(i -> tokens.get(i).type == TokenType.OPERATOR && tokens.get(i).operatorMaybe == '^')
                    .toArray();
            for(int i:expIndices){
                if(i == tokens.size() - 1) throw new ArithmeticException("Expression ended with a '^' unexpectedly");
                if(i == 0) throw new ArithmeticException("Expression cannot start with '^'");
                if(tokens.get(i+1).type != TokenType.VALUE || tokens.get(i-1).type != TokenType.VALUE) throw new ArithmeticException("'^' not used like an operator");
                double second = tokens.remove(i+1).valueMaybe;
                double first = tokens.remove(i-1).valueMaybe;
                tokens.set(i-1,new ArithmeticToken(Math.pow(first,second)));
            }

            int[] mulIndices = IntStream.range(0, tokens.size())
                    .filter(i -> tokens.get(i).type == TokenType.OPERATOR && tokens.get(i).operatorMaybe == '*')
                    .toArray();
            for(int i:mulIndices){
                if(i == tokens.size() - 1) throw new ArithmeticException("Expression ended with a '*' unexpectedly");
                if(i == 0) throw new ArithmeticException("Expression cannot start with '*'");
                if(tokens.get(i+1).type != TokenType.VALUE || tokens.get(i-1).type != TokenType.VALUE) throw new ArithmeticException("'*' not used like an operator");
                double second = tokens.remove(i+1).valueMaybe;
                double first = tokens.remove(i-1).valueMaybe;
                tokens.set(i-1,new ArithmeticToken(first*second));
            }

            int[] divIndices = IntStream.range(0, tokens.size())
                    .filter(i -> tokens.get(i).type == TokenType.OPERATOR && tokens.get(i).operatorMaybe == '/')
                    .toArray();
            for(int i:divIndices){
                if(i == tokens.size() - 1) throw new ArithmeticException("Expression ended with a '/' unexpectedly");
                if(i == 0) throw new ArithmeticException("Expression cannot start with '/'");
                if(tokens.get(i+1).type != TokenType.VALUE || tokens.get(i-1).type != TokenType.VALUE) throw new ArithmeticException("'/' not used like an operator");
                double second = tokens.remove(i+1).valueMaybe;
                double first = tokens.remove(i-1).valueMaybe;
                tokens.set(i-1,new ArithmeticToken(first/second));
            }
            int[] minIndices = IntStream.range(0, tokens.size())
                    .filter(i -> tokens.get(i).type == TokenType.OPERATOR && tokens.get(i).operatorMaybe == '-')
                    .toArray();
            for(int i:minIndices){
                if(i == tokens.size() - 1) throw new ArithmeticException("Expression ended with a '-' unexpectedly");
                if(tokens.get(i+1).type != TokenType.VALUE) throw new ArithmeticException("'-' not being used to specify a negative or subtract");
                if(i != 0 && tokens.get(i-1).type == TokenType.VALUE){
                    double second = tokens.remove(i+1).valueMaybe;
                    double first = tokens.remove(i-1).valueMaybe;
                    tokens.set(i-1,new ArithmeticToken(first-second));
                }
                else{
                    tokens.set(i,new ArithmeticToken(-tokens.remove(i+1).valueMaybe));
                }
            }

            int[] posIndices = IntStream.range(0, tokens.size())
                    .filter(i -> tokens.get(i).type == TokenType.OPERATOR && tokens.get(i).operatorMaybe == '+')
                    .toArray();
            LOGGER.log(Level.WARNING,Arrays.deepToString(Arrays.asList(posIndices).toArray()));
            for(int i:posIndices) {
                if(i == tokens.size() - 1) throw new ArithmeticException("Expression ended with a '+' unexpectedly");
                if(i == 0) throw new ArithmeticException("Expression cannot start with '+'");
                if(tokens.get(i+1).type != TokenType.VALUE || tokens.get(i-1).type != TokenType.VALUE) throw new ArithmeticException("'+' not used like an operator");
                double second = tokens.remove(i+1).valueMaybe;
                double first = tokens.remove(i-1).valueMaybe;
                tokens.set(i-1,new ArithmeticToken(first+second));
            }
            if(tokens.size()==1 && tokens.get(0).type == TokenType.VALUE) return tokens.get(0);
            else{
                //log before you throw this error
                LOGGER.log(Level.SEVERE, String.format("somehow '%s' turned into '%s' rather than resolving to a single value",this.expressionMaybe,Arrays.deepToString(tokens.toArray())));
                throw new Error("AAAAAAAAAAAAAAAAAAAAAAAAAAAH how did we get more than 2 tokens or a token that isn't a value??????");
            }

        }
    }
    /// Evaluates a mathematic expression recursively
    private double evalExpression(String expression, long creation) throws ArithmeticException {
        //original regex without string escapes [0-9+\-*\/^()\s]+
        if (!expression.matches("[0-9+\\-*\\/^()\\s\\.]+")) {
            throw new ArithmeticException("Invalid char(s) in expression only numbers, parentheses, +, -, * and / are allowed");
        }

        expression = expression.replaceAll("\\s","");

        int openParenCount = 0;
        int closeParenCount = 0;
        for (int i = 0; i < expression.length(); i++) {
            if (expression.charAt(i) == '(') openParenCount++;
            else if (expression.charAt(i) == ')') closeParenCount++;
        }
        if (openParenCount != closeParenCount) {
            throw new ArithmeticException("Parentheses must match up in expression");
        }

        return new ArithmeticToken(expression).evalToken(creation).valueMaybe;
    }
    @Override
    public void executeInternal(Message message, List<String> args) {
        String expression = String.join(" ",args);
        long creation = System.currentTimeMillis();
        double value = evalExpression(expression, creation);
        message.getChannel().sendMessage(
                String.format("the result of your expression `%s` is `%f`",
                    expression,
                    value)).queue();
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
        String[] aliases = {"m", "π"};
        return aliases;
    }

    @Override
    public boolean canBeDisabled() {
        return true;
    }
}