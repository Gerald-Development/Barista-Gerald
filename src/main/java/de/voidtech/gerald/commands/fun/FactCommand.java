package main.java.de.voidtech.gerald.commands.fun;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.json.JSONObject;

import main.java.de.voidtech.gerald.commands.AbstractCommand;
import net.dv8tion.jda.api.entities.Message;

public class FactCommand extends AbstractCommand 
{
	private static final String REQUEST_URL = "https://uselessfacts.jsph.pl/random.json?language=en";
	private static final Logger LOGGER = Logger.getLogger(FactCommand.class.getName());

	@Override
	public void executeInternal(Message message, List<String> args){
		
		try
		{
			URL requestURL = new URL(REQUEST_URL);
			HttpURLConnection con = (HttpURLConnection) requestURL.openConnection();
			con.setRequestMethod("GET");
			
			if (con.getResponseCode() == 200) {
				try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
					String content = in.lines().collect(Collectors.joining());
					JSONObject json = new JSONObject(content.toString());
					
					message.getChannel().sendMessage(json.getString("text")).queue();
				}
			}
			else
			{
				super.sendErrorOccurred();
			}
			
			con.disconnect();
		}
		catch(IOException e)
		{
			//TODO: Proper Error Handling
			LOGGER.log(Level.SEVERE, e.getMessage());
			super.sendErrorOccurred();
		}
	}

	@Override
	public String getDescription() {
		return "slaps some random facts on the table";
	}

	@Override
	public String getUsage() {
		return "fact";
	}

}
