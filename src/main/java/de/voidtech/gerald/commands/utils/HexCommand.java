package main.java.de.voidtech.gerald.commands.utils;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Command
public class HexCommand extends AbstractCommand {

	private static final String IMAGE_SOURCE_URL = "https://via.placeholder.com/250/";
	private static final String COLOUR_HEX_BASE_URL = "https://www.colorhexa.com/";
	private static final Logger LOGGER = Logger.getLogger(HexCommand.class.getName());
	
	private Color getEmbedColour(String hex) {
		return new Color(
	            Integer.valueOf(hex.substring(0, 2), 16),
	            Integer.valueOf(hex.substring(2, 4), 16),
	            Integer.valueOf(hex.substring(4, 6), 16));
	}
	
	private void sendHexImage(CommandContext context, String hexCode) {
		String finalImageURL = IMAGE_SOURCE_URL + hexCode + "/" + hexCode + ".png";
		String colourHexURL = COLOUR_HEX_BASE_URL + hexCode;
		String colourName = getColourName(colourHexURL);
		
		MessageEmbed hexColourEmbed = new EmbedBuilder()
				.setColor(getEmbedColour(hexCode))
				.setImage(finalImageURL)
				.setTitle("#" + hexCode.toUpperCase(), colourHexURL)
				.setFooter(colourName)
				.build();
		context.reply(hexColourEmbed);
	}
	
	private String getColourName(String colourHexURL) {
		try {
			Document colourHexPage = Jsoup.connect(colourHexURL).get();
			return colourHexPage.select("#information > div.color-description > p > strong").first().text();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error during CommandExecution: " + e.getMessage());
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		String hexCode = args.get(0).replaceAll("#", "");
		
		if (ParsingUtils.isHexadecimal(hexCode)) {
			sendHexImage(context, hexCode);
		} else {
			context.reply("**You did not supply a valid hex code!**");
		}
		
	}

	@Override
	public String getDescription() {
		return "This command allows you to see the color represented by a hex code! Simply provide a valid hex code (EG: FF00FF) and Gerald will show you the colour!";
	}

	@Override
	public String getUsage() {
		return "hex [6 character hex code]";
	}

	@Override
	public String getName() {
		return "hex";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.UTILS;
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
		return new String[]{"hexcolor", "colour", "hexcolour", "color"};
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}

}
