package main.java.de.voidtech.gerald.commands.utils;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.entities.JoinLeaveMessage;
import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.service.ServerService;
import main.java.de.voidtech.gerald.util.MRESameUserPredicate;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Command
public class WelcomerCommand extends AbstractCommand{

	@Autowired
	private ServerService serverService;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private EventWaiter waiter;

	private boolean customMessageEnabled(long guildID) {
		try(Session session = sessionFactory.openSession())
		{
			JoinLeaveMessage joinLeaveMessage = (JoinLeaveMessage) session.createQuery("FROM JoinLeaveMessage WHERE ServerID = :serverID")
                    .setParameter("serverID", guildID)
                    .uniqueResult();
			return joinLeaveMessage != null;
		}
	}
	
	private void deleteCustomMessage(long guildID) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			session.createQuery("DELETE FROM JoinLeaveMessage WHERE ServerID = :guildID")
				.setParameter("guildID", guildID)
				.executeUpdate();
			session.getTransaction().commit();
		}
	}
	
	private boolean channelExists (String channel, CommandContext context) {
		if (ParsingUtils.isInteger(channel)) {
			GuildChannel guildChannel = context.getJDA().getGuildChannelById(Long.parseLong(channel));
			return guildChannel != null;	
		} else {
			return false;
		}
	}
	
	private void addJoinLeaveMessage(long serverID, String channel, String joinMessage, String leaveMessage) {
		try (Session session = sessionFactory.openSession()) {
			session.getTransaction().begin();
		
			JoinLeaveMessage joinLeaveMessage = new JoinLeaveMessage(serverID, channel, joinMessage, leaveMessage);
			
			session.saveOrUpdate(joinLeaveMessage);
			session.getTransaction().commit();
		}
		
	}
	
	private JoinLeaveMessage getJoinLeaveMessageEntity(long guildID) {
		try(Session session = sessionFactory.openSession())
		{
			return (JoinLeaveMessage) session.createQuery("FROM JoinLeaveMessage WHERE ServerID = :serverID")
                    .setParameter("serverID", guildID)
                    .uniqueResult();
		}
	}
	
	private void updateChannel(long serverID, String channel) {
		JoinLeaveMessage joinLeaveMessage = getJoinLeaveMessageEntity(serverID);
		
		try (Session session = sessionFactory.openSession()) {
			session.getTransaction().begin();

			joinLeaveMessage.setChannelID(channel);
			
			session.saveOrUpdate(joinLeaveMessage);
			session.getTransaction().commit();
		}
	}
	
	private void updateJoinMessage(long serverID, String joinMessage) {
		JoinLeaveMessage joinLeaveMessage = getJoinLeaveMessageEntity(serverID);
		
		try (Session session = sessionFactory.openSession()) {
			session.getTransaction().begin();

			joinLeaveMessage.setJoinMessage(joinMessage);
			
			session.saveOrUpdate(joinLeaveMessage);
			session.getTransaction().commit();
		}
	}
	
	private void updateLeaveMessage(long serverID, String leaveMessage) {
		JoinLeaveMessage joinLeaveMessage = getJoinLeaveMessageEntity(serverID);
		
		try (Session session = sessionFactory.openSession()) {
			session.getTransaction().begin();

			joinLeaveMessage.setLeaveMessage(leaveMessage);
			
			session.saveOrUpdate(joinLeaveMessage);
			session.getTransaction().commit();
		}
	}
	
	private void clearWelcomer(Server server, CommandContext context) {
		if (customMessageEnabled(server.getId())) {
			deleteCustomMessage(server.getId());
			context.getChannel().sendMessage("**The Welcomer has been disabled.**").queue();
		} else {
			context.getChannel().sendMessage("**The Welcomer has not been set up yet!**").queue();
		}
	}
	
	private void continueToLeaveMessage(CommandContext context, Server server, String channel, String welcomeMessage) {
		context.getChannel().sendMessage("**Please enter your leave message:**").queue();
		waiter.waitForEvent(MessageReceivedEvent.class,
				new MRESameUserPredicate(context.getAuthor()),
				leaveMessageEvent -> {
					String leaveMessage = leaveMessageEvent.getMessage().getContentRaw();
					
					addJoinLeaveMessage(server.getId(), channel, welcomeMessage, leaveMessage);
					context.getChannel().sendMessage("**The Welcomer has been set up!**\n\n"
							+ "Channel: <#" + channel + ">\n"
							+ "Join message: " + welcomeMessage + "\n"
							+ "Leave message: " + leaveMessage).queue();
					
				}, 60, TimeUnit.SECONDS, 
				() -> context.getChannel().sendMessage("**No input has been supplied, cancelling.**").queue());
	}
	
	
	private void continueToWelcomeMessage(CommandContext context, Server server, String channel) {
		context.getChannel().sendMessage("**Please enter your welcome message:**").queue();
		waiter.waitForEvent(MessageReceivedEvent.class,
				new MRESameUserPredicate(context.getAuthor()),
				welcomeMessageInputEvent -> {
					String welcomeMessage = welcomeMessageInputEvent.getMessage().getContentRaw();
					continueToLeaveMessage(context, server, channel, welcomeMessage);
				}, 60, TimeUnit.SECONDS, 
				() -> context.getChannel().sendMessage("**No input has been supplied, cancelling.**").queue());
	}
	
	private void beginSetup(CommandContext context, Server server) {
		context.getChannel().sendMessage("**Enter the ID or a mention of the channel you wish to use:**").queue();
		
		waiter.waitForEvent(MessageReceivedEvent.class,
				new MRESameUserPredicate(context.getAuthor()),
				channelEntryEvent -> {
					String channel = ParsingUtils.filterSnowflake(channelEntryEvent.getMessage().getContentRaw());
					
					if (channelExists(channel, context)) {
						continueToWelcomeMessage(context, server, channel);
					} else {
						context.getChannel().sendMessage("**You need to mention a channel or use its ID!**").queue();
					}
					
				}, 60, TimeUnit.SECONDS, 
				() -> context.getChannel().sendMessage("**No input has been supplied, cancelling.**").queue());
	}
	
	private void setupWelcomer(Server server, CommandContext context) {
		if (customMessageEnabled(server.getId())) {
			context.getChannel().sendMessage("**The Welcomer is already set up!**").queue();
		} else {
			beginSetup(context, server);
		}	
	}
	
	private void changeChannel(Server server, CommandContext context, List<String> args) {
		if (customMessageEnabled(server.getId())) {
			String channel = ParsingUtils.filterSnowflake(args.get(1));
			
			if (channelExists(channel, context)) {
				updateChannel(server.getId(), channel);
				context.getChannel().sendMessage("**The channel has been changed to** <#" + channel + ">").queue();
			} else {
				context.getChannel().sendMessage("**You need to mention a channel or use its ID!**").queue();
			}
		} else {
			context.getChannel().sendMessage("**The Welcomer has not been set up yet! See below:\n\n**" + this.getUsage()).queue();
		}
	}
	
	private void changeWelcomeMessage(Server server, CommandContext context, List<String> args) {
		if (customMessageEnabled(server.getId())) {
			
			String joinMessage = "";
			
			for (int i = 1; i < args.size(); i++) {
				joinMessage = joinMessage + args.get(i);
			}
		
			updateJoinMessage(server.getId(), joinMessage);
			context.getChannel().sendMessage("**The join message has been changed to** " + joinMessage).queue();

		} else {
			context.getChannel().sendMessage("**The Welcomer has not been set up yet! See below:\n\n**" + this.getUsage()).queue();
		}
	}
	
	private void changeLeaveMessage(Server server, CommandContext context, List<String> args) {
		if (customMessageEnabled(server.getId())) {
			
			String leaveMessage = "";
			
			for (int i = 1; i < args.size(); i++) {
				leaveMessage = leaveMessage + args.get(i);
			}
			
			updateLeaveMessage(server.getId(), leaveMessage);
			context.getChannel().sendMessage("**The leave message has been changed to** " + leaveMessage).queue();

		} else {
			context.getChannel().sendMessage("**The Welcomer has not been set up yet! See below:\n\n**" + this.getUsage()).queue();
		}
	}
	
	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		
		Server server = serverService.getServer(context.getGuild().getId());
		switch(args.get(0)) {
		case "clear":
			clearWelcomer(server, context);
			break;
		
		case "setup":
			setupWelcomer(server, context);
			break;
		
		case "channel":
			changeChannel(server, context, args);
			break;
		
		case "joinmsg":
			changeWelcomeMessage(server, context, args);
			break;
		
		case "leavemsg":
			changeLeaveMessage(server, context, args);
			break;
		}
		
	}

	@Override
	public String getDescription() {
		return "This command allows you to set up a customiseable join/leave message system. Simply choose a channel, join message and leave message and you're ready! Note: You MUST seperate the arguments for this command with a dash (-) See usage for details";
	}

	@Override
	public String getUsage() {
		return "welcomer setup (then follow the steps you are shown)\n\n"
				+ "welcomer channel #welcome-new-members (to change the channel)\n\n"
				+ "welcomer joinmsg welcome to our server! (to change the welcome message)\n\n"
				+ "welcomer leavemsg we will miss you! (to change the leave message)\n\n"
				+ "welcomer clear";
	}

	@Override
	public String getName() {
		return "welcomer";
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
		return new String[]{"jm", "joinmessage"};
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}

}