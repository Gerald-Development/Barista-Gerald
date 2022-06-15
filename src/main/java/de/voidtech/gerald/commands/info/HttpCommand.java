package main.java.de.voidtech.gerald.commands.info;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.service.LogService;
import main.java.de.voidtech.gerald.util.GeraldLogger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Command
public class HttpCommand extends AbstractCommand {
	
	private static final String HTTP_CAT = "https://http.cat/";
	private static final GeraldLogger LOGGER = LogService.GetLogger(HttpCommand.class.getSimpleName());
	
	private static List<String> CatCodes = new ArrayList<String>();
	
	@EventListener(ApplicationReadyEvent.class)
	private void GetCatCodes() {
		try {
			Document httpCat = Jsoup.connect(HTTP_CAT).get();
			Elements catElements = httpCat.getElementsByClass("ThumbnailGrid_thumbnail__177T1");
			for (Element cat : catElements) {
				String url = cat.select("div > a").first().attr("href");
				List<String> urlParts = Arrays.asList(url.split("/"));
				String code = urlParts.get(urlParts.size() - 1);
				CatCodes.add(code);
			}
			LOGGER.logWithoutWebhook(Level.INFO, "Added http codes!");
		} catch (IOException e) {
			LOGGER.logWithoutWebhook(Level.SEVERE, "Failed to add http codes: " + e.getMessage());
		}
	}
	
	private boolean codeExists(String code) {
		return CatCodes.contains(code);
	}
	
	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		String code = codeExists(args.get(0)) ? args.get(0) : "404";
		String url = HTTP_CAT + code + ".jpg";
		MessageEmbed httpEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("HTTP " + code, url)
				.setImage(url)
				.build();
		context.reply(httpEmbed);
	}

	@Override
	public String getDescription() {
		return "Shows you the description of an HTTP code, along with a very helpful feline-based image.";
	}

	@Override
	public String getUsage() {
		return "http [http code]";
	}

	@Override
	public String getName() {
		return "http";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.INFO;
	}

	@Override
	public boolean isDMCapable() {
		return true;
	}

	@Override
	public boolean requiresArguments() {
		return true;
	}
	
	@Override
	public String[] getCommandAliases() {
		return new String[]{"httpcat", "httpcode"};
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