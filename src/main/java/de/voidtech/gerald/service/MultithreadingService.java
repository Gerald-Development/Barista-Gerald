package main.java.de.voidtech.gerald.service;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class MultithreadingService {

    private final HashMap<String, ExecutorService> threadMap = new HashMap<String, ExecutorService>();

    private ExecutorService findOrSpawnThread(String threadID) {

        if (!threadMap.containsKey(threadID)) {
            BasicThreadFactory factory = new BasicThreadFactory.Builder()
                    .namingPattern(threadID + "-%d")
                    .daemon(true)
                    .priority(Thread.NORM_PRIORITY)
                    .build();
            threadMap.put(threadID, Executors.newCachedThreadPool(factory));
        }

        return threadMap.get(threadID);
    }

    public ExecutorService getThreadByName(String name) {
        return findOrSpawnThread(name);
    }
}