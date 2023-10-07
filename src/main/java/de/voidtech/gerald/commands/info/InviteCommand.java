package main.java.de.voidtech.gerald.commands.info;

import main.java.de.voidtech.gerald.GlobalConstants;
import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.List;

@Command
public class InviteCommand extends AbstractCommand {

    @Override
    public void executeInternal(CommandContext context, List<String> args) {
        MessageEmbed inviteLinkEmbed = new EmbedBuilder()
                .setColor(Color.cyan)
                .setDescription("**[Add Gerald to your server!](" + GlobalConstants.INVITE_URL + ")**")
                .build();
        context.reply(inviteLinkEmbed);
    }

    @Override
    public String getDescription() {
        return "Gives you the Invite link for Gerald";
    }

    @Override
    public String getUsage() {
        return "invite";
    }

    @Override
    public String getName() {
        return "invite";
    }

    @Override
    public CommandCategory getCommandCategory() {
        return CommandCategory.INFO;
    }

    @Override
    public boolean isDMCapable() {
        return true;
    }

    @Override
    public boolean requiresArguments() {
        return false;
    }

    @Override
    public String[] getCommandAliases() {
        return new String[]{"inv", "link"};
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
