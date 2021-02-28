package main.java.de.voidtech.gerald.service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.springframework.stereotype.Service;

@Service
public class ChatbotService {	
	
	private Map<String, Chat> chatInstances = new HashMap<String, Chat>();
	
	private Chat getChatInstance(String userID) {
		if (chatInstances.containsKey(userID)) {
			return chatInstances.get(userID);	
		} else {
		    Chat chatSession = new Chat(new Bot("gerald", getResourcesPath()));
			chatInstances.put(userID, chatSession);
			return chatSession;
		}
	}
	
	public String getReply(String stimulus, String userID) {
	    return getChatInstance(userID).multisentenceRespond(stimulus);
	}
	
	private static String getResourcesPath() {
        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        path = path.substring(0, path.length() - 2);
        return path + File.separator + "src" + File.separator + "main" + File.separator + "resources";
    }
}