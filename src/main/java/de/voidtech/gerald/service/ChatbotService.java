package main.java.de.voidtech.gerald.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatbotService {
	
	@Autowired
	GeraldConfig configService;
	
	private static final Logger LOGGER = Logger.getLogger(ChatbotService.class.getName());
	private static final String ERROR_STRING = "I'm not sure how to respond to that.";
	
	private String getGavinResponse(String message) {
		String inputMessage = message;
		inputMessage = inputMessage.replaceAll("/", "");
		inputMessage = inputMessage.replaceAll(" ", "%20");
		String requestURL = configService.getGavinURL() + inputMessage;
		
		try {
            HttpURLConnection con = (HttpURLConnection) new URL(requestURL).openConnection();
            con.setRequestMethod("GET");

            if (con.getResponseCode() == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    return in.lines().collect(Collectors.joining());
                }
            }

            con.disconnect();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error during ServiceExecution: " + e.getMessage());
        }
        return ERROR_STRING;
	}
	
    public String getReply(String message, String ID) {
    	try {
			String gavinResponse = getGavinResponse(message);
			JSONObject responseObject = new JSONObject(gavinResponse);
			
			if (responseObject.has("error")) {
				return ERROR_STRING;
			} else {
				if (responseObject.has("message")) {
					return responseObject.getString("message");
				} else {
					return ERROR_STRING;
				}
			}
			
		} catch (JSONException e) {
			LOGGER.log(Level.SEVERE, "Error during ServiceExecution: " + e.getMessage());
		}
        return ERROR_STRING;
	}
}