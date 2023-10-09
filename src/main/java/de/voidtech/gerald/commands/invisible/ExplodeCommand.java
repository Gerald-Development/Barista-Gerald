package main.java.de.voidtech.gerald.commands.invisible;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.exception.HandledGeraldException;
import main.java.de.voidtech.gerald.exception.UnhandledGeraldException;

import java.util.List;

@Command
public class ExplodeCommand extends AbstractCommand {
    @Override
    public void executeInternal(CommandContext context, List<String> args) {
        switch (args.get(0)) {
            case "handled" -> throw new HandledGeraldException("This is a handled exception, don't panic");
            case "unhandled" -> throw new UnhandledGeraldException("Oh sweet jesus this is not handled");
            default -> context.reply("You need to choose handled or unhandled you dingus");
        }
    }

    @Override
    public String getDescription() {
        return "Blows up. Literally. Used for developer testing.";
    }

    @Override
    public String getUsage() {
        return "explode [handled/unhandled]";
    }

    @Override
    public String getName() {
        return "explode";
    }

    @Override
    public CommandCategory getCommandCategory() {
        return CommandCategory.INVISIBLE;
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
        return new String[]{"die"};
    }

    @Override
    public boolean canBeDisabled() {
        return false;
    }

    @Override
    public boolean isSlashCompatible() {
        return false;
    }
}
