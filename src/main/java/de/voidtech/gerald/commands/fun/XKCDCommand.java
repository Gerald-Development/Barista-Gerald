package main.java.de.voidtech.gerald.commands.fun;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import main.java.de.voidtech.gerald.util.RAESameUserPredicate;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Command
public class XKCDCommand extends AbstractCommand {
	
	@Autowired
	private EventWaiter waiter;
	
	private static final Logger LOGGER = Logger.getLogger(XKCDCommand.class.getName());
	
	private static final String XKCD_URL = "https://xkcd.com/";
	private static final String SUFFIX = "info.0.json";
	private static final String EMOTE_UNICODE = "U+1f440";
	
	private String makeRequest(String URL) {
		try {
			HttpURLConnection con = (HttpURLConnection) new URL(URL).openConnection();
			con.setRequestMethod("GET");
			con.disconnect();
			if (con.getResponseCode() == 200) {
				try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
					return in.lines().collect(Collectors.joining());
				}
			} else {
				return "";
			}
			
		} catch (IOException | JSONException e) {
			LOGGER.log(Level.SEVERE, "Error during CommandExecution: " + e.getMessage());
		}
		return "";
	}
	
	private String getCurrentXKCD() {
		return makeRequest(XKCD_URL + SUFFIX);
	}
	
	private String getXKCDById(int id) {
		return makeRequest(XKCD_URL + id + "/" + SUFFIX);
	}
	
	private void sendXKCD(JSONObject xkcd, CommandContext context) {
		String day = xkcd.get("day").toString();
		String month = xkcd.get("month").toString();
		String year = xkcd.get("year").toString();
		String title = xkcd.get("safe_title").toString();
		String alt = xkcd.get("alt").toString();
		String img = xkcd.get("img").toString();
		String num = xkcd.get("num").toString();
		
		MessageEmbed xkcdEmbed = new EmbedBuilder()
				.setColor(Color.CYAN)
				.setTitle(num + " - " + title, img)
				.setImage(img)
				.setFooter(day + "-" + month + "-" + year)
				.build();
		//TODO (from: Franziska): Queue and Waiter. Need to Inspect later.
		context.getChannel().sendMessageEmbeds(xkcdEmbed).queue(sentMessage -> {
			sentMessage.addReaction(EMOTE_UNICODE).queue();
			waiter.waitForEvent(MessageReactionAddEvent.class,
					new RAESameUserPredicate(context.getAuthor()),
					event -> {
					boolean moreInfoButtonPressed = event.getReactionEmote().toString().equals("RE:" + EMOTE_UNICODE);
					if (moreInfoButtonPressed) {
						MessageEmbed newXkcdEmbed = new EmbedBuilder()
								.setColor(Color.CYAN)
								.setTitle(num + " - " + title, img)
								.setDescription(alt)
								.setImage(img)
								.setFooter(day + "-" + month + "-" + year)
								.build();
						sentMessage.editMessageEmbeds(newXkcdEmbed).queue();
					}
				}, 30, TimeUnit.SECONDS, () -> {});
		});
		
	}
	
	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		if (args.size() == 0) {
			String response = getCurrentXKCD();
			String current = new JSONObject(response).get("num").toString();
			String randomResponse = getXKCDById(new Random().nextInt(Integer.parseInt(current)));
			sendXKCD(new JSONObject(randomResponse), context);
			
		} else if (args.get(0).equals("latest")) {
			String currentResponse = getCurrentXKCD();
			sendXKCD(new JSONObject(currentResponse), context);
			
		} else if (args.get(0).equals("id")) {
			if (ParsingUtils.isInteger(args.get(1))) {
				String byIdResponse = getXKCDById(Integer.parseInt(args.get(1)));
				if (byIdResponse.equals("")) {
					context.getChannel().sendMessage("**That ID could not be found!**").queue();
				
				} else {
					sendXKCD(new JSONObject(byIdResponse), context);
				}
				
			} else {
				context.getChannel().sendMessage("**That ID is not valid!**").queue();
			}
		}
	}

	@Override
	public String getDescription() {
		return "Gets a random XKCD comic, the latest XKCD comic, or an XKCD comic by id";
	}

	@Override
	public String getUsage() {
		return "xkcd\n"
				+ "xkcd latest\n"
				+ "xkcd id [id]";
	}

	@Override
	public String getName() {
		return "xkcd";
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
		return new String[]{"getxkcd"};
	}

	@Override
	public boolean canBeDisabled() {
		return true;
	}
	
}
