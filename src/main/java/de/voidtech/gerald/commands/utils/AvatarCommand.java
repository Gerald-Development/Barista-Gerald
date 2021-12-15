package main.java.de.voidtech.gerald.commands.utils;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;

@Command
public class AvatarCommand extends AbstractCommand{
	
	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		Member member = ParsingUtils.getMember(context, args);
		String avatarUrl = member.getUser().getAvatarUrl();
		if (avatarUrl == null)
			context.reply("**That user does not have an avatar!**");
		else {
			avatarUrl = avatarUrl + "?size=2048";
			MessageEmbed avatarEmbed = new EmbedBuilder()
					.setColor(member.getColor())
					.setTitle(member.getUser().getName() + "'s Avatar", avatarUrl)
					.setImage(avatarUrl)
					.build();
			context.reply(avatarEmbed);
		}
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
		return new String[]{"av", "pfp"};
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
