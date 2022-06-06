package main.java.de.voidtech.gerald.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.entities.GeraldLogger;

@Service
public class ChatbotService {
	
	@Autowired
	GeraldConfig configService;
	
	private static final GeraldLogger LOGGER = LogService.GetLogger(ChatbotService.class.getSimpleName());
	private static final String ERROR_STRING = "I'm not sure how to respond to that.";

	@NotNull
	private String httpMethodGET(URL requestURL) throws IOException {
		HttpURLConnection con = (HttpURLConnection) requestURL.openConnection();

		con.setRequestMethod("GET");
		con.setRequestProperty("Accept", "application/json");

		int responseCode = con.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_OK) {
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuilder response = new StringBuilder();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			return response.toString();
		}
		else {
			return ERROR_STRING;
		}
	}
	
	private String getGavinResponse(String message) {
		try {
			
			String payload = new JSONObject().put("data", message).toString();
			URL requestURL = new URL(configService.getGavinURL());
			HttpURLConnection con = (HttpURLConnection) requestURL.openConnection();
			
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json; utf-8");
			con.setDoOutput(true);
			con.setRequestProperty("Accept", "application/json");
			
			OutputStream os = con.getOutputStream();
			byte[] input = payload.getBytes(StandardCharsets.UTF_8);
			os.write(input, 0, input.length);
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));			
			String output = in.lines().collect(Collectors.joining());
			con.disconnect();
			
			return output;
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return ERROR_STRING;
	}

	private String getModelNameResponse() {
		try {
			URL requestURL = new URL(configService.getGavinURL()+"model_name");
			return httpMethodGET(requestURL);

		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return ERROR_STRING;
	}

	private String getHparamsResponse() {
		try {
			URL requestURL = new URL(configService.getGavinURL()+"hparams");
			return httpMethodGET(requestURL);
		} catch (Exception e1) {
			e1.printStackTrace();
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
	public JSONObject getHparams() {
		try {
			String hparamsResponse = getHparamsResponse();
			return new JSONObject(hparamsResponse);
		} catch (JSONException e) {
			LOGGER.log(Level.SEVERE, "Error during ServiceExecution: " + e.getMessage());
		}
		return new JSONObject().put("Error", ERROR_STRING);
	}
	public JSONObject getModelName() {
		try {
			String hparamsResponse = getModelNameResponse();
			return new JSONObject(hparamsResponse);
		} catch (JSONException e) {
			LOGGER.log(Level.SEVERE, "Error during ServiceExecution: " + e.getMessage());
		}
		return new JSONObject().put("Error", ERROR_STRING);
	}
}