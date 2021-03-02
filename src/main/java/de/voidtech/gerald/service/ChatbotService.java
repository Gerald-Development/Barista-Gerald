package main.java.de.voidtech.gerald.service;

import java.util.HashMap;
import java.util.Map;

import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.springframework.stereotype.Service;

@Service
public class ChatbotService {	
	
	private Map<String, Chat> chatInstances = new HashMap<String, Chat>();
<<<<<<< HEAD
	
	private Bot GERALD_AI = new Bot("gerald", "AIML");
=======
	private final String URI = getClass().getClassLoader().getResource("ai").getPath();
	private Bot geraldAI;
	
	public ChatbotService() {
		this.geraldAI = new Bot("Gerald", URI);
	}
>>>>>>> df632a14c1e263cdf41bda035487c530da5b456b
	
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
		String response = getChatInstance(userID).multisentenceRespond(stimulus); 
	    return response == "" ? "What?" : response;
	}
}