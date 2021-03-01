package main.java.de.voidtech.gerald.service;

import java.util.HashMap;
import java.util.Map;

import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.springframework.stereotype.Service;

@Service
public class ChatbotService {	
	
	private Map<String, Chat> chatInstances = new HashMap<String, Chat>();
	private final String URI = getClass().getClassLoader().getResource("ai").getPath();
	private Bot geraldAI;
	
	public ChatbotService() {
		this.geraldAI = new Bot("Gerald", URI);
	}
	
	private Chat getChatInstance(String userID) {
		if (chatInstances.containsKey(userID)) {
			return chatInstances.get(userID);	
		} else {
		    Chat chatSession = new Chat(geraldAI);
			chatInstances.put(userID, chatSession);
			return chatSession;
		}
	}
	
	public String getReply(String stimulus, String userID) {
	    return getChatInstance(userID).multisentenceRespond(stimulus);
	}
}