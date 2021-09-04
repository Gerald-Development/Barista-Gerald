package main.java.de.voidtech.gerald.commands.utils;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.entities.SuggestionChannel;
import main.java.de.voidtech.gerald.service.ServerService;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;

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
	
	private void addSuggestion(CommandContext context, List<String> args) {
		Server server = serverService.getServer(context.getGuild().getId());
		
		if (suggestionChannelExists(server.getId())) {
			String channelID = getChannelID(server.getId());

			//TODO (from: Franziska): We cannot supply the original message in case of SlashCommand. Needs some thought.
			MessageEmbed newSuggestionEmbed = new EmbedBuilder()
					.setColor(Color.ORANGE)
					.setTitle("New Suggestion!")
					.addField("Suggestion", String.join(" ", args), false)
					//-------->.addField("Original Message", "**[Click Here](" + context.getJumpUrl() + ")**", false)<--------
					.setFooter("Suggested By " + context.getAuthor().getAsTag(), context.getAuthor().getAvatarUrl())
					.build();					
			
			context.getGuild().getTextChannelById(channelID).sendMessageEmbeds(newSuggestionEmbed).queue(sentMessage -> {
				sentMessage.addReaction(CHECK).queue();
				sentMessage.addReaction(CROSS).queue();
				context.getChannel().sendMessage("**Your suggestion has been posted!**").queue();
			});
		} else {
			context.getChannel().sendMessage("**This command has not been set up yet!**\n\n"
					+ this.getUsage()).queue();
		}
		
	}

	private boolean isGuildChannel(String channelID, CommandContext context) {
		return context.getGuild().getTextChannelById(channelID) != null;
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
	
	private void validateInput(String channelID, Server server, CommandContext context) {
		if (ParsingUtils.isInteger(channelID)) {
			if (isGuildChannel(channelID, context)) {
				if (suggestionChannelExists(server.getId())) {
					updateSuggestionChannel(server.getId(), channelID);
					context.getChannel().sendMessage("**The suggestion channel has been updated!!**").queue();
				} else {
					addNewSuggestionChannel(server.getId(), channelID);
					context.getChannel().sendMessage("**The suggestion box has been set up!**").queue();
				}
			} else {
				context.getChannel().sendMessage("**That is not a valid text channel!**").queue();
			}
		} else {
			context.getChannel().sendMessage("**That is not a valid channel!**").queue();
		}		
	}
	
	private void setChannel(CommandContext context, List<String> args) {
		if (context.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
			if (args.size() < 2) {
				context.getChannel().sendMessage("**You need to specify a channel! Use a channel mention or its ID**").queue();
			} else {
				String channelID = ParsingUtils.filterSnowflake(args.get(1));
				Server server = serverService.getServer(context.getGuild().getId());
				
				validateInput(channelID, server, context);
			}
		} else {
			context.getChannel().sendMessage("**You do not have permission to do that!**").queue();
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

	private void disableSuggestions(CommandContext context) {
		if (context.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
			Server server = serverService.getServer(context.getGuild().getId());
			
			if (suggestionChannelExists(server.getId())) {
				deleteSuggestionChannel(server.getId());
				context.getChannel().sendMessage("**The suggestion system has been disabled**").queue();
			} else {
				context.getChannel().sendMessage("**The suggestion system has not yet been set up!**").queue();
			}	
		} else {
			context.getChannel().sendMessage("**You do not have permission to do that!**").queue();
		}
	}
	
	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		switch (args.get(0)) {
		case "channel":
			setChannel(context, args);
			break;
		case "disable":
			disableSuggestions(context);
			break;
		default:
			addSuggestion(context, args);
			break;
		}
	}

	@Override
	public String getDescription() {
		return "This command allows you to set up a suggestions box. Simply set the suggestion box channel and your users can start sending suggestions!";
			 //+ "If a suggestion is approved or disapproved, the red and green circle emotes can be used by members with the Manage Channel permission to change the embed color.";
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
		return new String[]{"idea", "suggestion"};
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}

}
