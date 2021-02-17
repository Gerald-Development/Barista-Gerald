package main.java.de.voidtech.gerald.service;

import java.io.IOException;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatbotService {
	
	@Autowired
	private GeraldConfig config;
	
    public String getReply(String message, String ID) {
    	String apiKey = config.getPersonalityForgeToken();
		String personalityForgeURL = "https://www.personalityforge.com/api/chat/?apiKey=" + apiKey + "&chatBotID=6&message=" + message + "&externalID=" + ID;
		
		try {
			Document doc = Jsoup.connect(personalityForgeURL).get();
			String jsonText = doc.select("body").text();
			
			JSONObject json = new JSONObject(jsonText.toString());	
			
			String reply = json.getJSONObject("message").getString("message");
			
			reply.replaceAll("<br>", "");
			reply.replaceAll("</br>", "");
			
			return reply;
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return "no message";
	}
}