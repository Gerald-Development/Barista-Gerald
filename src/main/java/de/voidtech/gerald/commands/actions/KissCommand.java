package main.java.de.voidtech.gerald.commands.actions;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;

import java.util.List;

@Command
public class KissCommand extends ActionsCommand {

    @Override
    public void executeInternal(CommandContext context, List<String> args) {
        super.sendAction(context, ActionType.KISS);
    }

    @Override
    public String getDescription() {
        return "Kiss a user!";
    }

    @Override
    public String getUsage() {
        return "kiss @user";
    }

    @Override
    public String getName() {
        return "kiss";
    }

    @Override
    public CommandCategory getCommandCategory() {
        return CommandCategory.ACTIONS;
    }

    @Override
    public boolean isDMCapable() {
        return false;
    }

    @Override
    public boolean requiresArguments() {
        return true;
    }

    @Override
    public String[] getCommandAliases() {
        return new String[]{"peck", "canoodle"};
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
