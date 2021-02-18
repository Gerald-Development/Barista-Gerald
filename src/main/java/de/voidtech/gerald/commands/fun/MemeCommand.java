package main.java.de.voidtech.gerald.commands.fun;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import net.dv8tion.jda.api.entities.Message;

@Command
public class MemeCommand extends AbstractCommand {
	
	private static final String API_URL = "https://fyouron-api.herokuapp.com/";
	
	private JSONObject assemblePayloadWithCaptions(List<String> args, String messageText) {
		List<String> captionsList = new ArrayList<String>(Arrays.asList(messageText.split("-")));
		String templateName = captionsList.get(0);
		
		captionsList.remove(0);
		
		JSONObject payload = new JSONObject();
		
		payload.put("template_name", templateName);
		payload.put("text", captionsList.toArray());
		
		return payload;		
	}
	
	private JSONObject assemblePayloadWithoutCaptions(String messageText) {
		JSONObject payload = new JSONObject();
		payload.put("template_name", messageText);
		return payload;
	}
	
	private String postPayload(JSONObject JSONPayload) {
		String payload = JSONPayload.toString();
		
		try {
			URL memeCommandURL = new URL(API_URL);
			HttpURLConnection con = (HttpURLConnection) memeCommandURL.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json; utf-8");
			con.setDoOutput(true);
			con.setRequestProperty("Accept", "application/json");
			
			try (OutputStream os = con.getOutputStream()) {
				byte[] input = payload.getBytes("utf-8");
				os.write(input, 0, input.length);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try (OutputStream os = con.getOutputStream(); BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
				
				String response = in.lines().collect(Collectors.joining());		
				
				return response.substring(1, response.length() - 1);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			con.disconnect();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return "template not found";
	}
	
	@Override
	public void executeInternal(Message message, List<String> args) {
		String messageText = String.join(" ", args);
		JSONObject payload = null;
		
		if (messageText.contains("-")) {
			payload = assemblePayloadWithCaptions(args, messageText);			
		} else {
			payload = assemblePayloadWithoutCaptions(messageText);
		}
		
		String apiResponse = postPayload(payload);
		if (apiResponse.equals("template not found")) {
			message.getChannel().sendMessage("Couldn't find that template :(").queue();
		} else {
			message.getChannel().sendMessage(apiResponse).queue();	
		}
	}

	@Override
	public String getDescription() {
		return "Allows you to request meme templates and add optional text to them. Note: caption text must be seperated by a '-'";
	}

	@Override
	public String getUsage() {
		return "meme [template name] [-text] [-text] .. [-text] ";
	}

	@Override
	public String getName() {
		return "meme";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public boolean isDMCapable() {
		return true;
	}

	@Override
	public boolean requiresArguments() {
		return true;
	}

}
