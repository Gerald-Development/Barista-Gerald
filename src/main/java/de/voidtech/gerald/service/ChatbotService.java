package main.java.de.voidtech.gerald.service;

import java.io.IOException;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class ChatbotService {
	
    public static String getReply(String message, String ID) {
    	String API_KEY = "mCTCtGMaZ86rmePA";
		String REQUEST_URL = "https://www.personalityforge.com/api/chat/?apiKey=" + API_KEY + "&chatBotID=6&message=" + message + "&externalID=" + ID;
		
		try {
			Document doc = Jsoup.connect(REQUEST_URL).get();
			String jsonText = doc.select("body").text();
			
			JSONObject json = new JSONObject(jsonText.toString());	
			
			String reply = json.getJSONObject("message").getString("message");
			
			return reply;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return "no message";
	}
	
}
