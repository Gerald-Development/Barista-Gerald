package main.java.de.voidtech.gerald.commands.utils;

import java.util.Arrays;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.entities.JoinLeaveMessage;
import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Message;

@Command
public class WelcomerCommand extends AbstractCommand{

	@Autowired
	private ServerService serverService;
	
	@Autowired
	private SessionFactory sessionFactory;

	private boolean customMessageEnabled(long guildID) {
		try(Session session = sessionFactory.openSession())
		{
			JoinLeaveMessage JLM = (JoinLeaveMessage) session.createQuery("FROM JoinLeaveMessage WHERE ServerID = :serverID")
                    .setParameter("serverID", guildID)
                    .uniqueResult();
			return JLM != null;
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

	private boolean isInt(String str) {
	    if (str == null) {
	        return false;
	    }
	    int length = str.length();
	    if (length == 0) {
	        return false;
	    }
	    int i = 0;
	    if (str.charAt(0) == '-') {
	        if (length == 1) {
	            return false;
	        }
	        i = 1;
	    }
	    for (; i < length; i++) {
	        char c = str.charAt(i);
	        if (c < '0' || c > '9') {
	            return false;
	        }
	    }
	    return true;
	}
	
	private boolean channelExists (String channel, Message message) {
		if (isInt(channel)) {
			GuildChannel guildChannel = message.getJDA().getGuildChannelById(Long.parseLong(channel));
			return guildChannel != null;	
		} else {
			return false;
		}
	}
	
	private boolean inputIsValid(List<String> arguments) {
		String argsString = String.join(" ", arguments);
	
		
		if (!argsString.contains("-")) {
			return false;
		}
		
		List<String> dashesCheckList = Arrays.asList(argsString.split("-"));
		if (dashesCheckList.size() < 3) {
			return false;	
		}
		
		String channel = arguments.get(1);
		channel = parseChannel(channel);
		
		if (channel.equals("")) {
			return false;
		}
		
		return true;
	}
	
	private String parseChannel(String inputString) {
		String output = inputString
			.replaceAll("<", "")
			.replaceAll("#", "")
			.replaceAll("!", "")
			.replaceAll(">", "");
		return output;
		
	}
	
	private void addJoinLeaveMessage(long serverID, String channel, String joinMessage, String leaveMessage) {
		try (Session session = sessionFactory.openSession()) {
			session.getTransaction().begin();
		
			JoinLeaveMessage JLM = new JoinLeaveMessage(serverID, channel, joinMessage, leaveMessage);
		
			JLM.setServerID(serverID);
			JLM.setChannelID(channel);
			JLM.setJoinMessage(joinMessage);
			JLM.setLeaveMessage(leaveMessage);
			
			session.saveOrUpdate(JLM);
			session.getTransaction().commit();
		}
		
	}
	
	private JoinLeaveMessage getJoinLeaveMessageEntity(long guildID) {
		try(Session session = sessionFactory.openSession())
		{
			JoinLeaveMessage JLM = (JoinLeaveMessage) session.createQuery("FROM JoinLeaveMessage WHERE ServerID = :serverID")
                    .setParameter("serverID", guildID)
                    .uniqueResult();
			return JLM;
		}
	}
	
	private void updateChannel(long serverID, String channel, Message message) {
		JoinLeaveMessage JLM = getJoinLeaveMessageEntity(serverID);
		
		try (Session session = sessionFactory.openSession()) {
			session.getTransaction().begin();

			JLM.setChannelID(channel);
			
			session.saveOrUpdate(JLM);
			session.getTransaction().commit();
		}
	}
	
	private void updateJoinMessage(long serverID, String joinMessage, Message message) {
		JoinLeaveMessage JLM = getJoinLeaveMessageEntity(serverID);
		
		try (Session session = sessionFactory.openSession()) {
			session.getTransaction().begin();

			JLM.setJoinMessage(joinMessage);
			
			session.saveOrUpdate(JLM);
			session.getTransaction().commit();
		}
	}
	
	private void updateLeaveMessage(long serverID, String leaveMessage, Message message) {
		JoinLeaveMessage JLM = getJoinLeaveMessageEntity(serverID);
		
		try (Session session = sessionFactory.openSession()) {
			session.getTransaction().begin();

			JLM.setLeaveMessage(leaveMessage);
			
			session.saveOrUpdate(JLM);
			session.getTransaction().commit();
		}
	}
	
	@Override
	public void executeInternal(Message message, List<String> args) {
		
		Server server = serverService.getServer(message.getGuild().getId());
		
		if (args.get(0).equals("clear")) {
			if (customMessageEnabled(server.getId())) {
				deleteCustomMessage(server.getId());
				message.getChannel().sendMessage("**The Welcomer has been disabled.**").queue();
			} else {
				message.getChannel().sendMessage("**The Welcomer has not been set up yet!**").queue();
			}
		} else if (args.get(0).equals("set")) {
			if (customMessageEnabled(server.getId())) {
				message.getChannel().sendMessage("**The Welcomer is already set up!**").queue();
			} else {
				if (inputIsValid(args)) {
					String channel = parseChannel(args.get(1));
					
					if (channelExists(channel, message)) {
						List<String> commandArgs = Arrays.asList(String.join(" ", args).split("-"));
						String joinMessage = commandArgs.get(1);
						String leaveMessage = commandArgs.get(2);
						addJoinLeaveMessage(server.getId(), channel, joinMessage, leaveMessage);
						message.getChannel().sendMessage("**The Welcomer has been set up!**\n\n"
								+ "Channel: <#" + channel + ">\n"
								+ "Join message: " + joinMessage + "\n"
								+ "Leave message: " + leaveMessage).queue();
				
					} else {
						message.getChannel().sendMessage("**You need to mention a channel or use its ID!**").queue();
					}
					
				} else {
					message.getChannel().sendMessage("**Your arguments haven't been set up right:\n\nExample: **" + this.getUsage()).queue();
				}	
			}
		} else if (args.get(0).equals("channel")) {
			if (customMessageEnabled(server.getId())) {
				String channel = parseChannel(args.get(1));
				
				if (channelExists(channel, message)) {
					updateChannel(server.getId(), channel, message);
					message.getChannel().sendMessage("**The channel has been changed to** <#" + channel + ">").queue();
				} else {
					message.getChannel().sendMessage("**You need to mention a channel or use its ID!**").queue();
				}
			} else {
				message.getChannel().sendMessage("**The Welcomer has not been set up yet! See below:\n\n**" + this.getUsage()).queue();
			}
		} else if (args.get(0).equals("joinmsg")) {
			if (customMessageEnabled(server.getId())) {
				
				String joinMessage = "";
				
				for (int i = 1; i < args.size(); i++) {
					joinMessage = joinMessage + args.get(i);
				}
			
				updateJoinMessage(server.getId(), joinMessage, message);
				message.getChannel().sendMessage("**The join message has been changed to** " + joinMessage).queue();

			} else {
				message.getChannel().sendMessage("**The Welcomer has not been set up yet! See below:\n\n**" + this.getUsage()).queue();
			}
		} else if (args.get(0).equals("leavemsg")) {
			if (customMessageEnabled(server.getId())) {
				
				String leaveMessage = "";
				
				for (int i = 1; i < args.size(); i++) {
					leaveMessage = leaveMessage + args.get(i);
				}
				
				updateLeaveMessage(server.getId(), leaveMessage, message);
				message.getChannel().sendMessage("**The leave message has been changed to** " + leaveMessage).queue();

			} else {
				message.getChannel().sendMessage("**The Welcomer has not been set up yet! See below:\n\n**" + this.getUsage()).queue();
			}
		}
		
	}

	@Override
	public String getDescription() {
		return "This command allows you to set up a customiseable join/leave message system. Simply choose a channel, join message and leave message and you're ready! Note: You MUST seperate the arguments for this command with a dash (-) See usage for details";
	}

	@Override
	public String getUsage() {
		return "welcomer set #welcome -has joined the server! -has left the server :(\n"
				+ "welcomer channel #welcome-new-members\n"
				+ "welcomer joinmsg welcome to our server!\n"
				+ "welcomer leavemsg we will miss you!\n"
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

}