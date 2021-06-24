package main.java.de.voidtech.gerald.commands.utils;

import java.util.List;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Command
public class AvatarCommand extends AbstractCommand{
	
	@Override
	public void executeInternal(Message message, List<String> args) {
		Member member = ParsingUtils.getMember(message, args);	
		String avatarUrl = member.getUser().getAvatarUrl() + "?size=2048";
		MessageEmbed avatarEmbed = new EmbedBuilder()
				.setColor(member.getColor())
				.setTitle(member.getUser().getName() + "'s Avatar", avatarUrl)
				.setImage(avatarUrl)
				.build();
		message.getChannel().sendMessage(avatarEmbed).queue();
	}

	@Override
	public String getDescription() {
		return "Allows you to view your avatar or somebody else's";
	}

	@Override
	public String getUsage() {
		return "avatar\n"
				+ "avatar [user id]\n"
				+ "avatar [@user#1234]";
	}

	@Override
	public String getName() {
		return "avatar";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.UTILS;
	}

	@Override
	public boolean isDMCapable() {
		return false;
	}

	@Override
	public boolean requiresArguments() {
		return false;
	}

	@Override
	public String[] getCommandAliases() {
		String[] aliases = {"av", "pfp"};
		return aliases;
	}

	@Override
	public boolean canBeDisabled() {
		return true;
	}
	
}
