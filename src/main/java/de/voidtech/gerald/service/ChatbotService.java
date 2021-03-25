package main.java.de.voidtech.gerald.service;

import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.stereotype.Service;

@Service
public class ChatbotService {

	//TODO REVIEW: This is not a constant. Variable name is not according to code conventions.
	private Bot Gerald_AI = null;
	//TODO REVIEW: This is not a constant. Variable name is not according to code conventions.
	private ExecutorService Chat_Thread = null;
	private HashMap<String, Chat> chatInstances = new HashMap<String, Chat>();
	
	private static final Logger LOGGER = Logger.getLogger(ChatbotService.class.getName());
	
	private Chat getChatInstance(String sessionID) {
		
		if (!chatInstances.containsKey(sessionID)) {
			chatInstances.put(sessionID, new Chat(Gerald_AI));
		}
		return chatInstances.get(sessionID);
	}
	
	private ExecutorService getChatThread() {
		if (Chat_Thread == null) {
			 BasicThreadFactory factory = new BasicThreadFactory.Builder()
				     .namingPattern("GeraldAI-%d")
				     .daemon(true)
				     .priority(Thread.NORM_PRIORITY)
				     .build();
			Chat_Thread = Executors.newSingleThreadExecutor(factory);
		}	
		return Chat_Thread;		
	}
	
	public int getSessionCount() {
		return chatInstances.size();
	}
	
	public boolean isChatInitialised() {
		return Chat_Thread != null;
	}
	
    public String getReply(String message, String ID) {
        try {
        	ExecutorService aiResponseThreadExecutor = getChatThread();
            Callable<String> responseThreadCallable = new Callable<String>() {
                @Override
                public String call() {
                	if (Gerald_AI == null) {
                		GeraldConfig config = new GeraldConfig();
                		Gerald_AI = new Bot("Gerald", config.getAIMLFolderDirectory());
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
