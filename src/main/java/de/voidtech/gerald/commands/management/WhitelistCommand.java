package main.java.de.voidtech.gerald.commands.management;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

//TODO: What if channel gets deleted?
public class WhitelistCommand extends AbstractCommand 
{
	private ServerService serverService;

	@Override
	public void executeInternal(Message message, List<String> args) {
		this.serverService = ServerService.getInstance();

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
		if (mentionedChannel != null) {
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
		
		message.getChannel().sendMessage(String.format("Gerald can be used in the following channels:\n%s",
				StringUtils.join(channelList, "\n"))).queue();
	}
	
	@Override
	public String getDescription() {
		return "manages the whitelist of the server";
	}

	@Override
	public String getUsage() {
		return "whitelist add {channelID}\nwhitelist remove {channelID}\nwhitelist clear\nwhitelist";
	}

}
