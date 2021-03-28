package main.java.de.voidtech.gerald.service;

import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatbotService {

	@Autowired
	ThreadManager threadManager;
	
	private Bot geraldAI = null;
	private HashMap<String, Chat> chatInstances = new HashMap<String, Chat>();
	
	private static final Logger LOGGER = Logger.getLogger(ChatbotService.class.getName());
	
	private Chat getChatInstance(String sessionID) {
		
		if (!chatInstances.containsKey(sessionID)) {
			chatInstances.put(sessionID, new Chat(geraldAI));
		}
		return chatInstances.get(sessionID);
	}
	
    public String getReply(String message, String ID) {
        try {
        	ExecutorService aiResponseThreadExecutor = threadManager.getThreadByName("Gerald-AI");
            Callable<String> responseThreadCallable = new Callable<String>() {
                @Override
                public String call() {
                	if (geraldAI == null) {
                		GeraldConfig config = new GeraldConfig();
                		geraldAI = new Bot("Gerald", config.getAIMLFolderDirectory());
                	}
                	
                	String reply = getChatInstance(ID).multisentenceRespond(message);
                	return reply == "" ? "What?" : reply;
                }
            };
            Future<String> responseAwaiter = aiResponseThreadExecutor.submit(responseThreadCallable);
			return responseAwaiter.get();
		} catch (InterruptedException | ExecutionException e) {
			LOGGER.log(Level.SEVERE, "Error during ServiceExecution: " + e.getMessage());
		}
        
        return "What?";
	}
	}
