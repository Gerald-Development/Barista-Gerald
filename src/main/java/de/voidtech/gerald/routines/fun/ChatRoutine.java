package main.java.de.voidtech.gerald.routines.fun;

import main.java.de.voidtech.gerald.entities.ChatChannelRepository;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Routine;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import main.java.de.voidtech.gerald.routines.RoutineCategory;
import main.java.de.voidtech.gerald.service.ChatbotService;
import net.dv8tion.jda.api.entities.Message;

@Routine
public class ChatRoutine extends AbstractRoutine{
	
	@Autowired
	private ChatbotService chatBot;

	@Autowired
	private ChatChannelRepository repository;
	
	private boolean chatChannelEnabled(String channelID) {
		return repository.getChatChannelByChannelId(channelID) != null;
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
		return "Allows channels with the chat command to work. Communicates with our AI chatbot";
	}

	@Override
	public boolean allowsBotResponses() {
		return false;
	}

	@Override
	public boolean canBeDisabled() {
		return false;
	}

	@Override
	public String getName() {
		return "r-chat";
	}
	
	@Override
	public RoutineCategory getRoutineCategory() {
		return RoutineCategory.FUN;
	}
}
