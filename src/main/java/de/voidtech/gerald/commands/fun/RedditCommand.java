package main.java.de.voidtech.gerald.commands.fun;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.service.LogService;
import main.java.de.voidtech.gerald.util.GeraldLogger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Command
public class RedditCommand extends AbstractCommand {
	
	private static final String BASE_URL = "https://www.reddit.com/r/";
	private static final String SUFFIX = "/top/.json?t=all&limit=10";
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.2; WOW64; Trident/7.0; rv:11.0) like Gecko";
	private static final GeraldLogger LOGGER = LogService.GetLogger(RedditCommand.class.getSimpleName());
	private static final String NO_IMAGE_URL = "https://cdn.discordapp.com/attachments/727233195380310016/850452145810964500/no_image_here_buster.gif";
	
	private JSONObject getRedditData(String fullUrl) {
		try {
			HttpURLConnection con = (HttpURLConnection) new URL(fullUrl).openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", USER_AGENT);

			if (con.getResponseCode() == 200) {
				try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
					JSONObject response = new JSONObject(in.lines().collect(Collectors.joining()));
					con.disconnect();
					return response; 
				}
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error during CommandExecution: " + e.getMessage());
		}
		return null;
	}
	
	private int getUpvotes(JSONObject post) {
		return post.getJSONObject("data").getInt("ups");
	}

	private String getTitle(JSONObject post) {
		return post.getJSONObject("data").getString("title");
	}

	private String getImage(JSONObject post) {
		String url;
		if (post.getJSONObject("data").has("url_overridden_by_dest")) {
			url = post.getJSONObject("data").getString("url_overridden_by_dest");
		} else {
			url = NO_IMAGE_URL;
		}
		return url; 
	}
	
	private boolean isNsfw(JSONObject post) {
		return post.getJSONObject("data").getBoolean("over_18");
	}
	
	private boolean channelIsNsfw(CommandContext context) {
		return ((TextChannel)context.getChannel()).isNSFW();
	}
	
	private void sendRedditMessage(CommandContext context, JSONObject redditData) {
		
		JSONArray posts = redditData.getJSONObject("data").getJSONArray("children");
		
		if (posts.length() == 0) {
			context.reply("**That is not a valid subreddit!**");
		} else {
			JSONObject chosenPost = posts.getJSONObject(new Random().nextInt(posts.length()));
			
			String imageURL = getImage(chosenPost);
			String title = getTitle(chosenPost);
			int upvotes = getUpvotes(chosenPost);
			
			if (isNsfw(chosenPost) && !channelIsNsfw(context)) {
				context.reply("**This post cannot be displayed as it contains 18+ content**");
			} else {
				MessageEmbed redditEmbed = new EmbedBuilder()
						.setColor(Color.ORANGE)
						.setTitle(title, imageURL)
						.setImage(imageURL)
						.setFooter("Upvotes: " + upvotes)
						.build();
				context.reply(redditEmbed);
			}	
		}
	}

	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		String subreddit = args.get(0);
		String fullUrl = BASE_URL + subreddit + SUFFIX;
		
		JSONObject redditData = getRedditData(fullUrl);
		
		if (redditData == null) {
			context.reply("**Something  wrong!**");
		} else {
			sendRedditMessage(context, redditData);
		}
	}

	@Override
	public String getDescription() {
		return "A command for the people that are into memes and reddit, this allows you to supply a name of a subreddit and have a random post picked from the top 20 recent posts.";
	}

	@Override
	public String getUsage() {
		return "reddit [subreddit name]";
	}

	@Override
	public String getName() {
		return "reddit";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public boolean isDMCapable() {
		return false;
	}

	@Override
	public boolean requiresArguments() {
		return true;
	}

	@Override
	public String[] getCommandAliases() {
		return new String[]{"subreddit","sub"};
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
