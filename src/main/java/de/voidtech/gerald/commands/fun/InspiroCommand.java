package main.java.de.voidtech.gerald.commands.fun;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Command
public class InspiroCommand extends AbstractCommand{
	private static final String REQUEST_URL = "https://inspirobot.me/api?generate=true";
	private static final String INSPIRO_ICON = "https://inspirobot.me/website/images/inspirobot-dark-green.png";
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.2; WOW64; Trident/7.0; rv:11.0) like Gecko";
	private static final Logger LOGGER = Logger.getLogger(InspiroCommand.class.getName());

	@Override
	public void executeInternal(Message message, List<String> args) {
		String inspiroImageURLOpt = getInspiroImageURLOpt();
		if (inspiroImageURLOpt != null)
		{
			MessageEmbed inspiroEmbed = new EmbedBuilder()//
					.setTitle("InspiroBot says:", inspiroImageURLOpt)//
					.setColor(Color.ORANGE)//
					.setImage(inspiroImageURLOpt)//
					.setFooter("Data from InspiroBot", INSPIRO_ICON)//
					.build();
			message.getChannel().sendMessage(inspiroEmbed).queue();
		}
	}
	
	private String getInspiroImageURLOpt() {
		try {
			HttpURLConnection con = (HttpURLConnection) new URL(REQUEST_URL).openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", USER_AGENT);

			if (con.getResponseCode() == 200) {
				try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
					return in.lines().collect(Collectors.joining());
				}
			}
			con.disconnect();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error during CommandExecution: " + e.getMessage());
		}
		return null;
	}

	@Override
	public String getDescription() {
		return "sends a very inspiring picture";
	}

	@Override
	public String getUsage() {
		return "inspiro";
	}

	@Override
	public String getName() {
		return "inspiro";
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
		return false;
	}
	
	@Override
	public String[] getCommandAliases() {
		String[] aliases = {};
		return aliases;
	}

}
