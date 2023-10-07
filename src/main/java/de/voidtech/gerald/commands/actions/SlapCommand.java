package main.java.de.voidtech.gerald.commands.actions;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;

import java.util.List;

@Command
public class SlapCommand extends ActionsCommand {

    @Override
    public void executeInternal(CommandContext context, List<String> args) {
        super.sendAction(context, ActionType.SLAP);
    }

    @Override
    public String getDescription() {
        return "Slap a user!";
    }

    @Override
    public String getUsage() {
        return "slap @user";
    }

    @Override
    public String getName() {
        return "slap";
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
        return new String[]{"smack", "hit", "punch", "kill"};
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
