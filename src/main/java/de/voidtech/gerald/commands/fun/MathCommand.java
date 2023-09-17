package main.java.de.voidtech.gerald.commands.fun;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;

import java.util.*;

import static main.java.de.voidtech.gerald.util.ArithmeticUtils.evalExpression;

@Command
public class MathCommand extends AbstractCommand {

    @Override
    public void executeInternal(CommandContext context, List<String> args) {
        String expression = String.join(" ",args);
        try {
            double result = evalExpression(expression, System.currentTimeMillis());
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
