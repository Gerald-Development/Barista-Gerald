package main.java.de.voidtech.gerald.commands.fun;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.entities.ChatChannel;
import main.java.de.voidtech.gerald.service.ChatbotService;
import net.dv8tion.jda.api.entities.Message;

@Command
public class ChatCommand extends AbstractCommand{
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private ChatbotService chatBot;
	
	private boolean chatChannelEnabled(String channelID) {
		try(Session session = sessionFactory.openSession())
		{
			ChatChannel channel = (ChatChannel) session.createQuery("FROM ChatChannel WHERE ChannelID = :channelID")
                    .setParameter("channelID", channelID)
                    .uniqueResult();
			return channel != null;
		}
	}
	
	private void enableChatChannel(String channelID) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			
			ChatChannel channel = new ChatChannel(channelID);
			
			channel.setChatChannel(channelID);
			
			session.saveOrUpdate(channel);
			session.getTransaction().commit();
		}
	}
	
	private void disableChatChannel(String channelID) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			session.createQuery("DELETE FROM ChatChannel WHERE ChannelID = :channelID")
				.setParameter("channelID", channelID)
				.executeUpdate();
			session.getTransaction().commit();
		}
	}
	
	@Override
	public void executeInternal(Message message, List<String> args) {
		if (args.get(0).equals("enable")) {
			if (chatChannelEnabled(message.getChannel().getId())) {
				message.getChannel().sendMessage("**GeraldAI is already enabled here!**").queue();
			} else {
				enableChatChannel(message.getChannel().getId());
				message.getChannel().sendMessage("**GeraldAI has been enabled!**").queue();
			}
		} else if (args.get(0).equals("disable")) {
			if (chatChannelEnabled(message.getChannel().getId())) {
				disableChatChannel(message.getChannel().getId());
				message.getChannel().sendMessage("**GeraldAI has been disabled!**").queue();
			} else {
				message.getChannel().sendMessage("**GeraldAI is already disabled!**").queue();
			}
		} else {
			message.getChannel().sendTyping().queue();
			String reply = chatBot.getReply(String.join(" ", args), message.getGuild().getId());
			message.getChannel().sendMessage(reply).queue();
		}
		
	}

	@Override
	public String getDescription() {
		return "This command allows you to talk to our Chat AI! (Powered by Gavin)";
	}
	@Override
	public String getUsage() {
		return "chat enable / chat disable";
	}

	@Override
	public String getName() {
		return "chat";
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
		String[] aliases = {"ai", "geraldai", "geraldchat"};
		return aliases;
	}

}
