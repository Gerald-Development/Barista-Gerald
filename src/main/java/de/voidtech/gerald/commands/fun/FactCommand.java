package main.java.de.voidtech.gerald.commands.fun;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;

import main.java.de.voidtech.gerald.commands.AbstractCommand;
import net.dv8tion.jda.api.entities.Message;

public class FactCommand extends AbstractCommand 
{

	@Override
	public void executeInternal(Message message, List<String> args) throws IOException {
		
		URL requestURL = new URL("https://uselessfacts.jsph.pl/random.json?language=en");
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

	@Override
	public String getDescription() {
		return "slaps some random facts on the table";
	}

	@Override
	public String getUsage() {
		return "fact";
	}

}
