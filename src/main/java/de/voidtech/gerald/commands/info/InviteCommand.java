package main.java.de.voidtech.gerald.commands.info;

import java.awt.Color;
import java.util.List;

import main.java.de.voidtech.gerald.GlobalConstants;
import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Command
public class InviteCommand extends AbstractCommand {

	@Override
	public void executeInternal(Message message, List<String> args) {
		MessageEmbed inviteLinkEmbed = new EmbedBuilder()
				.setColor(Color.cyan)
				.setDescription("**[Gerald Invite Link](" + GlobalConstants.INVITE_URL + ")**")
				.build();
		message.getChannel().sendMessage(inviteLinkEmbed).queue();
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
		String[] aliases = {};
		return aliases;
	}

}
