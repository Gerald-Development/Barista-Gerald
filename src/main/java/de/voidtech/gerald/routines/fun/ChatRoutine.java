package main.java.de.voidtech.gerald.routines.fun;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Routine;
import main.java.de.voidtech.gerald.entities.ChatChannel;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import main.java.de.voidtech.gerald.routines.RoutineCategory;
import main.java.de.voidtech.gerald.service.ChatbotService;
import net.dv8tion.jda.api.entities.Message;

@Routine
public class ChatRoutine extends AbstractRoutine{

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
	
	@Override
	public void executeInternal(Message message) {
		if (message.getContentRaw().contains("chat enable") | message.getContentRaw().contains("chat disable")) {
			return;
		}
		
		if (chatChannelEnabled(message.getChannel().getId())) {
			message.getChannel().sendTyping().queue();
			String reply = chatBot.getReply(message.getContentRaw(), message.getGuild().getId());
			message.getChannel().sendMessage(reply).queue();
		}
	}

	@Override
	public String getDescription() {
		return "GeraldAI Routine";
	}

	@Override
	public boolean allowsBotResponses() {
		return false;
	}

	@Override
	public String getName() {
		return "GeraldAI";
	}
	
	@Override
	public RoutineCategory getRoutineCategory() {
		return RoutineCategory.FUN;
	}
}
