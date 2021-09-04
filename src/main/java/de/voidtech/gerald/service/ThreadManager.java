package main.java.de.voidtech.gerald.service;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.stereotype.Service;

@Service
public class ThreadManager {
	
	private final HashMap<String, ExecutorService> threadMap = new HashMap<String, ExecutorService>();

	private ExecutorService findOrSpawnThread(String threadID) {
		
		if (!threadMap.containsKey(threadID)) {
			BasicThreadFactory factory = new BasicThreadFactory.Builder()
				     .namingPattern(threadID + "-%d")
				     .daemon(true)
				     .priority(Thread.NORM_PRIORITY)
				     .build();
			threadMap.put(threadID, Executors.newSingleThreadExecutor(factory));	
		}
		
		return threadMap.get(threadID);
	}
	
	public ExecutorService getThreadByName(String name) {
		return findOrSpawnThread(name);
	}
}
