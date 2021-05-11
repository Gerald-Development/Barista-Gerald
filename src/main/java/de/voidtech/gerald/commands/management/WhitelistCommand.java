package main.java.de.voidtech.gerald.commands.management;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

@Command
public class WhitelistCommand extends AbstractCommand 
{
	@Autowired
	private ServerService serverService;

	@Override
	public void executeInternal(Message message, List<String> args) {
		
		if(!message.getMember().hasPermission(Permission.MANAGE_SERVER)) return;
		
		String argString = args.size() > 0 ? args.get(0) : "list";
		TextChannel mentionedChannel = message.getMentionedChannels().size() > 0 
				? message.getMentionedChannels().get(0) 
				: null;
		
		Server server = serverService.getServer(message.getGuild().getId());

		switch (argString) {
		case "add":
			handleAddToWhitelist(message, mentionedChannel, server);
			break;
		case "remove":
			handleRemoveFromWhitelist(message, mentionedChannel, server);
			break;
		case "clear":
			server.clearChannelWhitelist();
			message.getChannel().sendMessage("Whitelist has been cleared.").queue();
			break;
		case "list":
			handleListWhitelist(message, server);
			break;
		default:
			message.getChannel().sendMessage(String.format("Please specify the command.\n```%s```", getUsage()))
					.queue();
		}

		serverService.saveServer(server);
	}


	private void handleAddToWhitelist(Message message, TextChannel mentionedChannel, Server server) 
	{
		if (mentionedChannel != null && mentionedChannel.getGuild().getIdLong() == message.getGuild().getIdLong()) {
			if (server.getChannelWhitelist().contains(mentionedChannel.getId()))
				message.getChannel().sendMessage("This channel has already been added to the whitelist").queue();
			else {
				server.addToChannelWhitelist(mentionedChannel.getId());
				message.getChannel()
						.sendMessage("Channel has been added to the whitelist: " + mentionedChannel.getAsMention())
						.queue();
			}
		} else message.getChannel().sendMessage("Please provide a valid channel.").queue();
	}
	
	private void handleRemoveFromWhitelist(Message message, TextChannel mentionedChannel, Server server) 
	{
		if (mentionedChannel != null) {
			if (!server.getChannelWhitelist().contains(mentionedChannel.getId()))
				message.getChannel().sendMessage("This channel is not on the whitelist.").queue();
			else {
				server.removeFromChannelWhitelist(mentionedChannel.getId());
				message.getChannel()
				.sendMessage(
						"Channel has been removed from the whitelist: " + mentionedChannel.getAsMention())
				.queue();
			}
		} else message.getChannel().sendMessage("Please provide a valid channel.").queue();
	}
	
	private void handleListWhitelist(Message message, Server server) {
		List<String> channelList = message.getGuild()
				.getTextChannels()
				.stream()
				.filter(channel -> server.getChannelWhitelist().contains(channel.getId()))
				.map(channel -> channel.getAsMention())
				.collect(Collectors.toList());
		
		String usableChannels = StringUtils.join(channelList, "\n") == "" ? "All Channels" : StringUtils.join(channelList, "\n");
		
		message.getChannel().sendMessage(String.format("Gerald can be used in the following channels:\n%s",
				usableChannels)).queue();
	}
	
	@Override
	public String getDescription() {
		return "manages the whitelist of the server";
	}

	@Override
	public String getUsage() {
		return "whitelist add {channelID}\nwhitelist remove {channelID}\nwhitelist clear\nwhitelist";
	}

	@Override
	public String getName() {
		return "whitelist";
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
		return false;
	}
	
	@Override
	public String[] getCommandAliases() {
		String[] aliases = {};
		return aliases;
	}

}
