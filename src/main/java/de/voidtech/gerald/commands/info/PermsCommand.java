package main.java.de.voidtech.gerald.commands.info;

import java.awt.Color;
import java.util.EnumSet;
import java.util.List;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Command
public class PermsCommand extends AbstractCommand{
	
	@Override
	public void executeInternal(Message message, List<String> args) {
		EnumSet<Permission> perms;
		String member = "";
		if (args.size() > 0) {
			if (args.get(0).equals("everyone")) {
				perms = message.getGuild().getPublicRole().getPermissions();
				member = "Everyone";
			} else {
				perms = ParsingUtils.getMember(message, args).getPermissions((GuildChannel) message.getChannel());
				member = ParsingUtils.getMember(message, args).getUser().getAsTag();
			}	
		} else {
			perms = ParsingUtils.getMember(message, args).getPermissions((GuildChannel) message.getChannel());
			member = ParsingUtils.getMember(message, args).getUser().getAsTag();
		}
		
		sendPermsEmbed(message, perms, member);
	}

	private void sendPermsEmbed(Message message, EnumSet<Permission> perms, String member) {
		String permsList = "";
		for (Permission perm : perms) {
			permsList += perm.getName() + "\n";
		}
		MessageEmbed permissionsListEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("Permissions for " + member)
				.setDescription("```\n" + permsList + "\n```")
				.build();
		message.getChannel().sendMessage(permissionsListEmbed).queue();
		
	}

	@Override
	public String getDescription() {
		return "For server debugging and more, allows you to check someone's (or your own) permissions in a server.";
	}

	@Override
	public String getUsage() {
		return "perms [@member#1234/ID/everyone]";
	}

	@Override
	public String getName() {
		return "perms";
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
		return false;
	}

	@Override
	public String[] getCommandAliases() {
		String[] aliases = {"permissions", "permsin"};
		return aliases;
	}

}
