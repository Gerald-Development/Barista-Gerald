package main.java.de.voidtech.gerald.service;
import org.springframework.stereotype.Service;


@Service
public class ChatbotService {


    public String getReply(String contentRaw, String id) {
        return "This feature is under maintenance at the moment - we're hoping to bring something new and interesting soon!";
    }

    //TODO: Ollama?

}