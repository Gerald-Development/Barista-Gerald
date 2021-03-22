package main.java.de.voidtech.gerald.service;

import java.util.HashMap;

import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.springframework.stereotype.Service;

@Service
public class ChatbotService {

	private Bot Gerald_AI = null;

	private HashMap<String, Chat> chatInstances = new HashMap<String, Chat>();
	
	private Chat getChatInstance(String userID) {
		
		if (!chatInstances.containsKey(userID)) {
			chatInstances.put(userID, new Chat(Gerald_AI));
		}
		return chatInstances.get(userID);
	}
	
    public String getReply(String message, String ID) {
    	
    	if (Gerald_AI == null) {
    		GeraldConfig config = new GeraldConfig();
    		Gerald_AI = new Bot("Gerald", config.getAIMLFolderDirectory());
    	}
    	
    	String reply = getChatInstance(ID).multisentenceRespond(message);
    	return reply == "" ? "What?" : reply;
	}
	}
