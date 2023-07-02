package main.java.de.voidtech.gerald.commands.fun;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.awt.*;
import java.util.List;

@Command
public class VoteCommand extends AbstractCommand{
	private final static Emoji CHECK = Emoji.fromUnicode("U+2705");
	private final static Emoji CROSS = Emoji.fromUnicode("U+274C");
	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		if (context.isSlash()) context.reply("Vote created!");
		context.getChannel().sendMessageEmbeds(constructVoteEmbed(context, String.join(" ", args))).queue(selfMessage -> {
			selfMessage.addReaction(CHECK).queue();
			selfMessage.addReaction(CROSS).queue();
		});
	}
	
	private MessageEmbed constructVoteEmbed(CommandContext context, String voteTopic) {
		return new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("Vote!")
				.setDescription(voteTopic)
				.setFooter("Requested By " + context.getAuthor().getEffectiveName(), context.getAuthor().getAvatarUrl())
				.build();
	}

	@Override
	public String getDescription() {
		return "Allows you to hold a quick vote on your very sensitive topics";
	}

	@Override
	public String getUsage() {
		return "vote is this democracy?";
	}

	@Override
	public String getName() {
		return "vote";
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
		return new String[]{"poll"};
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}
	
	@Override
	public boolean isSlashCompatible() {
		return false;
	}

}