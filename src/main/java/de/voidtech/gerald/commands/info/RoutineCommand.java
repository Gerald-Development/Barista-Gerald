package main.java.de.voidtech.gerald.commands.info;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

@Command
public class RoutineCommand extends AbstractCommand {
    @Override
    public void executeInternal(Message message, List<String> args) {

    }

    @Override
    public String getDescription() {
        return "List all routines";
    }

    @Override
    public String getUsage() {
        return "routine";
    }

    @Override
    public String getName() {
        return "routine";
    }

    @Override
    public CommandCategory getCommandCategory() {
        return CommandCategory.INFO;
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
        String[] aliases = {"routines", "r"};
        return aliases;
    }
}
