package main.java.de.voidtech.gerald.commands.utils;

import java.awt.Color;
import java.util.List;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Command
public class HexCommand extends AbstractCommand {

	private static final String IMAGE_SOURCE_URL = "https://via.placeholder.com/250/";
	
	private Color getEmbedColour(String hex) {
		return new Color(
	            Integer.valueOf(hex.substring(0, 2), 16),
	            Integer.valueOf(hex.substring(2, 4), 16),
	            Integer.valueOf(hex.substring(4, 6), 16));
	}
	
	private void sendHexImage(Message message, String hexCode) {
		String finalImageURL = IMAGE_SOURCE_URL + hexCode + "/" + hexCode + ".png";
		MessageEmbed hexColourEmbed = new EmbedBuilder()
				.setColor(getEmbedColour(hexCode))
				.setImage(finalImageURL)
				.setTitle(hexCode.toUpperCase(), finalImageURL)
				.build();
		message.getChannel().sendMessage(hexColourEmbed).queue();
	}
	
	@Override
	public void executeInternal(Message message, List<String> args) {
		String hexCode = args.get(0).replaceAll("#", "");
		
		if (ParsingUtils.isHexadecimal(hexCode)) {
			sendHexImage(message, hexCode);
		} else {
			message.getChannel().sendMessage("**You did not supply a valid hex code!**").queue();
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
		String[] aliases = {"hexcolor", "colour", "hexcolour", "color"};
		return aliases;
	}

}
