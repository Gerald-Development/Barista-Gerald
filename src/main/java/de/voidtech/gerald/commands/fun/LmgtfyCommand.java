package main.java.de.voidtech.gerald.commands.fun;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;

import java.util.List;

@Command
public class LmgtfyCommand extends AbstractCommand {

    private static final String GOOGLE_QUERY = "https://www.google.com/search?q=";

    @Override
    public void executeInternal(CommandContext context, List<String> args) {
        String replyURL = "Follow this: " + GOOGLE_QUERY + String.join("+", args);
        context.getChannel().sendMessage(replyURL).queue();
    }

    @Override
    public String getDescription() {
        return "LMGTFY (Let Me Google That For You), when someone asks a very simple question that google can answer. Use this.";
    }

    @Override
    public String getUsage() {
        return "lmgtfy [search]";
    }

    @Override
    public String getName() {
        return "lmgtfy";
    }

    @Override
    public CommandCategory getCommandCategory() {
        return CommandCategory.FUN;
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
        String[] aliases = {"letmegetthat", "letmegoogle"};
        return aliases;
    }
    
	@Override
	public boolean canBeDisabled() {
		return true;
	}
}
