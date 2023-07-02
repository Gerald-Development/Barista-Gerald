package main.java.de.voidtech.gerald.commands.fun;

import main.java.de.voidtech.gerald.util.EventWaiter;
import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.service.LogService;
import main.java.de.voidtech.gerald.util.GeraldLogger;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import main.java.de.voidtech.gerald.util.RAESameUserPredicate;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
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
import java.util.stream.Collectors;

@Command
public class XKCDCommand extends AbstractCommand {
	
	@Autowired
	private EventWaiter waiter;
	
	private static final GeraldLogger LOGGER = LogService.GetLogger(XKCDCommand.class.getSimpleName());
	
	private static final String XKCD_URL = "https://xkcd.com/";
	private static final String SEARCH_URL = "https://search-xkcd.mfwowocringe.repl.co/search/";
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
			} else return "";
		} catch (IOException | JSONException e) {
			LOGGER.log(Level.SEVERE, "Error during CommandExecution: " + e.getMessage());
		}
		return "";
	}
	
	private String getXKCDBySearch(String search) {
		String result = makeRequest(SEARCH_URL + search);
		if (result.startsWith("404")) return "";
		else return getXKCDById(new JSONObject(result).getInt("id"));
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
			sentMessage.addReaction(Emoji.fromUnicode(EMOTE_UNICODE)).queue();
			waiter.waitForEvent(MessageReactionAddEvent.class,
					new RAESameUserPredicate(context.getAuthor()),
					event -> {
					boolean moreInfoButtonPressed = event.getEmoji().asUnicode().getAsCodepoints().equals(EMOTE_UNICODE);
					if (moreInfoButtonPressed) {
						MessageEmbed newXkcdEmbed = new EmbedBuilder()
								.setColor(Color.CYAN)
								.setTitle(num + " - " + title, img)
								.setDescription(alt)
								.setImage(img)
								.setFooter(day + "-" + month + "-" + year + " | Requested by " +
										context.getAuthor().getName() + "#" + context.getAuthor().getDiscriminator())
								.build();
						sentMessage.editMessageEmbeds(newXkcdEmbed).queue();
					}
				}, 30, TimeUnit.SECONDS, () -> {});
		});
		
	}
	
	private String formSearch(List<String> args) {
		if (args.size() == 1) return "";
		else {
			StringBuilder buffer = new StringBuilder();
			for (int i = 1; i < args.size(); i++) buffer.append(args.get(i)).append("+");
			return buffer.toString();
		}
	}
	
	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		if (args.size() == 0) {
			String response = getCurrentXKCD();
			String current = new JSONObject(response).get("num").toString();
			String randomResponse = getXKCDById(new Random().nextInt(Integer.parseInt(current)));
			sendXKCD(new JSONObject(randomResponse), context);
		} else {
			switch (args.get(0)) {
			case "latest":
				String currentResponse = getCurrentXKCD();
				sendXKCD(new JSONObject(currentResponse), context);
				break;
			case "id":
				if (ParsingUtils.isInteger(args.get(1))) {
					String byIdResponse = getXKCDById(Integer.parseInt(args.get(1)));
					if (byIdResponse.equals("")) context.reply("**That ID could not be found!**");
					else sendXKCD(new JSONObject(byIdResponse), context);
				} else context.reply("**That ID is not valid!**");
				break;
			case "search":
				String search = formSearch(args);
				if (search.equals("")) context.reply("**Your search was invalid!**");
				else {
					String searchResult = getXKCDBySearch(search);
					if (searchResult.equals("")) context.reply("**Your search returned no results!**");
					else sendXKCD(new JSONObject(searchResult), context);
				}
				break;
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
				+ "xkcd id [id]\n"
				+ "xkcd search [search string]";
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
	
	@Override
	public boolean isSlashCompatible() {
		return true;
	}
	
}