package main.java.de.voidtech.gerald.commands.info;

import java.awt.Color;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.service.GeraldConfig;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

@Command
public class CacheSearchCommand extends AbstractCommand{
	
	@Autowired
	private GeraldConfig config;
	
	@Autowired
	private ServerService serverService;
	
	@Override
	public void executeInternal(Message message, List<String> args) {
		if (config.getMasters().contains(message.getAuthor().getId())) {
			JDA client = message.getJDA();
			String resultMessage = "";
			String imageURL = "";
			boolean foundItem = true;
			
			if (client.getUserById(args.get(0)) != null) {
				User user = client.retrieveUserById(args.get(0)).complete();
				resultMessage = "**User Cache**\n```\n"
				+ "Username: " + user.getAsTag() 
				+ "\nUser ID: " + user.getId() 
				+ "```";
				imageURL = user.getAvatarUrl(); 
				
			} else if (client.getGuildById(args.get(0)) != null) {
				Guild guild = client.getGuildById(args.get(0));
				Member owner = guild.retrieveOwner().complete();
				resultMessage = "**Guild Cache**\n```\n"
						+ "Guild Name: " + guild.getName()
				+ "\nGuild ID: " + guild.getId()
				+ "\nGuild Owner: " + owner.getUser().getAsTag()
				+ "\nGuild Owner ID: " + owner.getId()
				+ "\nBarista Guild ID: " + serverService.getServer(guild.getId()).getId()
				+ "```";
				imageURL = guild.getIconUrl() ;
				
			} else if (client.getGuildChannelById(args.get(0)) != null) {
				GuildChannel channel = client.getGuildChannelById(args.get(0));
				resultMessage = "**Channel Cache**\n```\n"
				+ "Channel Name: " + channel.getName()
				+ "\nChannel Type: " + channel.getType()
				+ "\nChannel ID: " + channel.getId()
				+ "\nGuild Name: " + channel.getGuild().getName()
				+ "\nGuild ID: " + channel.getGuild().getId()
				+ "\nBarista Guild ID: " + serverService.getServer(channel.getGuild().getId()).getId() 
				+ "```";
				imageURL = channel.getGuild().getIconUrl();
			} else {
				foundItem = false;
			}
			
			if (foundItem) {
				MessageEmbed cacheSearchEmbed = new EmbedBuilder()
						.setColor(Color.ORANGE)
						.setThumbnail(imageURL)
						.setDescription(resultMessage)
						.build();
				message.getChannel().sendMessage(cacheSearchEmbed).queue();	
			} else {
				message.getChannel().sendMessage("**Nothing was found in the cache!**").queue();
			}
		}
	}

	@Override
	public String getDescription() {
		return "Allows bot masters to search the cache of members, channels and guilds";
	}

	@Override
	public String getUsage() {
		return "cachesearch [snowflake]";
	}

	@Override
	public String getName() {
		return "cachesearch";
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
		return true;
	}

}
