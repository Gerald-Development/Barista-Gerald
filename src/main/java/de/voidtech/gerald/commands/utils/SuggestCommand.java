package main.java.de.voidtech.gerald.commands.utils;

import java.awt.Color;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.entities.SuggestionChannel;
import main.java.de.voidtech.gerald.service.ServerService;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Command
public class SuggestCommand extends AbstractCommand {

	@Autowired
	private ServerService serverService;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	private final static String CHECK = "U+2705";
	private final static String CROSS = "U+274C";
	
	private boolean suggestionChannelExists(long serverID) {
		try(Session session = sessionFactory.openSession())
		{
			SuggestionChannel suggestionChannel = (SuggestionChannel) session.createQuery("FROM SuggestionChannel WHERE ServerID = :serverID")
                    .setParameter("serverID", serverID)
                    .uniqueResult();
			return suggestionChannel != null;
		}
	}
	
	private String getChannelID(long serverID) {
		try(Session session = sessionFactory.openSession())
		{
			SuggestionChannel suggestionChannel = (SuggestionChannel) session.createQuery("FROM SuggestionChannel WHERE ServerID = :serverID")
                    .setParameter("serverID", serverID)
                    .uniqueResult();
			return suggestionChannel.getSuggestionChannel();
		}
	}
	
	private void addSuggestion(Message message, List<String> args) {
		Server server = serverService.getServer(message.getGuild().getId());
		
		if (suggestionChannelExists(server.getId())) {
			String channelID = getChannelID(server.getId());
			
			MessageEmbed newSuggestionEmbed = new EmbedBuilder()
					.setColor(Color.ORANGE)
					.setTitle("New Suggestion!")
					.addField("Suggestion", String.join(" ", args), false)
					.addField("Original Message", "**[Click Here](" + message.getJumpUrl() + ")**", false)
					.setFooter("Suggested By " + message.getAuthor().getAsTag(), message.getAuthor().getAvatarUrl())
					.build();					
			
			message.getGuild().getTextChannelById(channelID).sendMessageEmbeds(newSuggestionEmbed).queue(sentMessage -> {
				sentMessage.addReaction(CHECK).queue();
				sentMessage.addReaction(CROSS).queue();
				message.getChannel().sendMessage("**Your suggestion has been posted!**").queue();
			});
		} else {
			message.getChannel().sendMessage("**This command has not been set up yet!**\n\n"
					+ this.getUsage()).queue();
		}
		
	}

	private boolean isGuildChannel(String channelID, Message message) {
		return message.getGuild().getTextChannelById(channelID) != null;
	}
	
	private void addNewSuggestionChannel(long serverID, String channelID) {
		try (Session session = sessionFactory.openSession()) {
			session.getTransaction().begin();
		
			SuggestionChannel suggestionChannel = new SuggestionChannel(serverID, channelID);			
			session.saveOrUpdate(suggestionChannel);
			session.getTransaction().commit();
		}		
	}
	
	private void updateSuggestionChannel(long serverID, String channelID) {
		try(Session session = sessionFactory.openSession())
		{
			SuggestionChannel suggestionChannel = (SuggestionChannel) session.createQuery("FROM SuggestionChannel WHERE ServerID = :serverID")
                    .setParameter("serverID", serverID)
                    .uniqueResult();
			
			suggestionChannel.setSuggestionChannel(channelID);
			
			session.getTransaction().begin();			
			session.saveOrUpdate(suggestionChannel);
			session.getTransaction().commit();
		}
		
	}
	
	private void validateInput(String channelID, Server server, Message message) {
		if (ParsingUtils.isInteger(channelID)) {
			if (isGuildChannel(channelID, message)) {
				if (suggestionChannelExists(server.getId())) {
					updateSuggestionChannel(server.getId(), channelID);
					message.getChannel().sendMessage("**The suggestion channel has been updated!!**").queue();
				} else {
					addNewSuggestionChannel(server.getId(), channelID);
					message.getChannel().sendMessage("**The suggestion box has been set up!**").queue();
				}
			} else {
				message.getChannel().sendMessage("**That is not a valid text channel!**").queue();
			}
		} else {
			message.getChannel().sendMessage("**That is not a valid channel!**").queue();
		}		
	}
	
	private void setChannel(Message message, List<String> args) {
		if (message.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
			if (args.size() < 2) {
				message.getChannel().sendMessage("**You need to specify a channel! Use a channel mention or its ID**").queue();
			} else {
				String channelID = ParsingUtils.filterSnowflake(args.get(1));
				Server server = serverService.getServer(message.getGuild().getId());
				
				validateInput(channelID, server, message);
			}
		} else {
			message.getChannel().sendMessage("**You do not have permission to do that!**").queue();
		}
		
	}

	private void deleteSuggestionChannel(long guildID) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			session.createQuery("DELETE FROM SuggestionChannel WHERE ServerID = :guildID")
				.setParameter("guildID", guildID)
				.executeUpdate();
			session.getTransaction().commit();
		}
	}

	private void disableSuggestions(Message message, List<String> args) {
		if (message.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
			Server server = serverService.getServer(message.getGuild().getId());
			
			if (suggestionChannelExists(server.getId())) {
				deleteSuggestionChannel(server.getId());
				message.getChannel().sendMessage("**The suggestion system has been disabled**").queue();
			} else {
				message.getChannel().sendMessage("**The suggestion system has not yet been set up!**").queue();
			}	
		} else {
			message.getChannel().sendMessage("**You do not have permission to do that!**").queue();
		}
	}
	
	@Override
	public void executeInternal(Message message, List<String> args) {
		switch (args.get(0)) {
		case "channel":
			setChannel(message, args);
			break;
		case "disable":
			disableSuggestions(message, args);
			break;
		default:
			addSuggestion(message, args);
			break;
		}
	}

	@Override
	public String getDescription() {
		return "This command allows you to set up a suggestions box. Simply set the suggestion box channel and your users can start sending suggestions!";
			 //+ "If a suggesion is approved or disapproved, the red and green circle emotes can be used by members with the Manage Channel permission to change the embed color.";
	}

	@Override
	public String getUsage() {
		return "suggest channel [channel]\n"
			 + "suggest disable\n"
			 + "suggest [suggestion]";
	}

	@Override
	public String getName() {
		return "suggest";
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
		return true;
	}
	
	@Override
	public String[] getCommandAliases() {
		String[] aliases = {"idea", "suggestion"};
		return aliases;
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}

}
