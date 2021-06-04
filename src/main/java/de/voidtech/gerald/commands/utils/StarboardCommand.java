package main.java.de.voidtech.gerald.commands.utils;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.entities.StarboardConfig;
import main.java.de.voidtech.gerald.service.ServerService;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

@Command
public class StarboardCommand extends AbstractCommand {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private ServerService serverService;
	
	//TODO: REVIEW DB Queries in Command?! 
	private boolean starboardConfigExists(long serverID) {
		try(Session session = sessionFactory.openSession())
		{
			StarboardConfig config = (StarboardConfig) session.createQuery("FROM StarboardConfig WHERE ServerID = :serverID")
                    .setParameter("serverID", serverID)
                    .uniqueResult();
			return config != null;
		}
	}
	//TODO: REVIEW DB Queries in Command?! 
	private StarboardConfig getStarboardConfig(long serverID) {
		try(Session session = sessionFactory.openSession())
		{
			StarboardConfig config = (StarboardConfig) session.createQuery("FROM StarboardConfig WHERE ServerID = :serverID")
                    .setParameter("serverID", serverID)
                    .uniqueResult();
			return config;
		}	
	}
	//TODO: REVIEW DB Queries in Command?! 
	private void deleteStarboardConfig(Message message, Server server) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			session.createQuery("DELETE FROM StarboardConfig WHERE ServerID = :serverID")
				.setParameter("serverID", server.getId())
				.executeUpdate();
			session.getTransaction().commit();
			message.getChannel().sendMessage("**The Starboard has been disabled. You will need to run setup again if you wish to undo this! Your starred messages will not be lost.**").queue();
		}
	}
	//TODO: REVIEW DB Queries in Command?! 
	private void updateConfig(StarboardConfig config) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();			
			session.saveOrUpdate(config);
			session.getTransaction().commit();
		}		
	}
	//TODO: REVIEW DB Queries in Command?! 
	private void completeStarboardSetup(Message message, String channelID, String starCount, Server server) {
		int requiredStarCount = Integer.parseInt(starCount);
		
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			
			StarboardConfig config = new StarboardConfig(server.getId(), channelID, requiredStarCount);
			
			session.saveOrUpdate(config);
			session.getTransaction().commit();
			
			message.getChannel().sendMessage("**Starboard setup complete!**\n"
					+ "Channel: <#" + channelID + ">\n"
					+ "Stars required: " + starCount + "\n"
					+ "Users must use the :star: emote!").queue();
		}
	}
	
	private void setupStarboard(Message message, List<String> args, Server server) {
		if (starboardConfigExists(server.getId())) {
			message.getChannel().sendMessage("**A Starboard has already been set up here! Did you mean to use one of these?**\n\n" + this.getUsage()).queue();
			//TODO: REVIEW if you have an if directly after an else then just do else if(boolean){} and not else { if(boolean) {} }
		} else {
			if (args.size() < 3) {
				message.getChannel().sendMessage("**You need more arguments than that!**\n\n" + this.getUsage()).queue();
			} else {
				String channelID = ParsingUtils.filterSnowflake(args.get(1));
				if (!ParsingUtils.isSnowflake(channelID)) {
					message.getChannel().sendMessage("**The channel you provided is not valid!**").queue();
				} else {
					if (!ParsingUtils.isInteger(args.get(2))) {
						message.getChannel().sendMessage("**You need to specify a number for the star count!**").queue();
					} else {
						completeStarboardSetup(message, channelID, args.get(2), server);
					}
				}
			}
		}
	}
	
	private void disableStarboard(Message message, Server server) {
		if (starboardConfigExists(server.getId())) {
			deleteStarboardConfig(message, server);
		} else {
			message.getChannel().sendMessage("**A Starboard has not been set up here! Did you mean to use one of these?**\n\n" + this.getUsage()).queue();	
		}
	}

	private void changeChannel(Message message, List<String> args, Server server) {
		if (starboardConfigExists(server.getId())) {
			String channelID = ParsingUtils.filterSnowflake(args.get(1));
			if (!ParsingUtils.isSnowflake(channelID)) {
				message.getChannel().sendMessage("**The channel you provided is not valid!**").queue();
			} else {
				StarboardConfig config = getStarboardConfig(server.getId());
				config.setChannelID(channelID);
				updateConfig(config);
			}
		} else {
			message.getChannel().sendMessage("**A Starboard has not been set up here! Did you mean to use one of these?**\n\n" + this.getUsage()).queue();	
		}
	}

	private void changeRequiredStarCount(Message message, List<String> args, Server server) {
		if (starboardConfigExists(server.getId())) {
			if (!ParsingUtils.isInteger(args.get(1))) {
				message.getChannel().sendMessage("**You need to specify a number for the star count!**").queue();
			} else {
				StarboardConfig config = getStarboardConfig(server.getId());
				config.setRequiredStarCount(Integer.parseInt(args.get(1)));
				updateConfig(config);
			}
		} else {
			message.getChannel().sendMessage("**A Starboard has not been set up here! Did you mean to use one of these?**\n\n" + this.getUsage()).queue();	
		}
	}
	
	@Override
	public void executeInternal(Message message, List<String> args) {
		if (message.getMember().getPermissions().contains(Permission.MANAGE_CHANNEL)) {
			Server server = serverService.getServer(message.getGuild().getId());
			
			switch (args.get(0)) {
			case "setup":
				setupStarboard(message, args, server);
				break;
			case "count":
				changeRequiredStarCount(message, args, server);
				break;
			case "channel":
				changeChannel(message, args, server);
				break;
			case "disable":
				disableStarboard(message, server);
				break;
			default:
				message.getChannel().sendMessage("**That's not a valid subcommand! Try this instead:**\n\n" + this.getUsage()).queue();
			}
		} else {
			message.getChannel().sendMessage("**You need the ** `Manage Channels` **Permission to do that!**").queue();
		}		
	}

	@Override
	public String getDescription() {
		return "Do you like quoting things? Funny, interesting and more? Perfect!\n"
				+ "Our starboard system allows you to react to messages with the :star: emote and have them automatically sent to"
				+ " a starboard channel in your server! Your server admins can choose the channel and number of stars needed to get it pinned!";
	}

	@Override
	public String getUsage() {
		return "starboard setup [Channel mention / ID] [Required star count]\n"
				+ "starboard count [New number of stars needed]\n"
				+ "starboard channel [New channel mention / ID]\n"
				+ "starboard disable\n\n"
				+ "NOTE: You MUST run the setup command first!";
	}

	@Override
	public String getName() {
		return "starboard";
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
		String[] aliases = {"autoquote", "quotechannel", "sb"};
		return aliases;
	}

}
