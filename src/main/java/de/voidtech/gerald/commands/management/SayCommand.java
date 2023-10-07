package main.java.de.voidtech.gerald.commands.management;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import net.dv8tion.jda.api.Permission;

import java.util.List;

@Command
public class SayCommand extends AbstractCommand {

    @Override
    public void executeInternal(CommandContext context, List<String> args) {
        if (context.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            String msg = String.join(" ", args);
            context.getChannel().sendMessage(msg).queue(response -> context.getMessage().delete().queue());
        }
    }

    @Override
    public String getDescription() {
        return "repeats the message you type";
    }

    @Override
    public String getUsage() {
        return "say a very exciting message";
    }

    @Override
    public String getName() {
        return "say";
    }

    @Override
    public CommandCategory getCommandCategory() {
        return CommandCategory.MANAGEMENT;
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
        return new String[]{"speak", "blessuswiththevoiceofgerald"};
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