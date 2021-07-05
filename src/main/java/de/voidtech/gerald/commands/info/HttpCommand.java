package main.java.de.voidtech.gerald.commands.info;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Command
public class HttpCommand extends AbstractCommand {

	private static final String HTTP_CAT = "https://http.cat/";
	private static final String CODES = 
			  "100,101,102,200,201,202,"
			+ "204,206,207,300,301,302,"
			+ "303,304,305,307,308,400,"
			+ "401,402,403,404,405,406,"
			+ "408,409,410,411,412,413,"
			+ "414,415,416,417,418,420,"
			+ "421,422,423,424,425,426,"
			+ "429,431,444,450,451,499,"
			+ "500,501,502,503,504,506,"
			+ "507,508,509,510,511,599";
	
	private boolean codeExists(String code) {
		List<String> codes = Arrays.asList(CODES.split(","));
		return codes.contains(code);
	}
	
	@Override
	public void executeInternal(Message message, List<String> args) {
		String code = args.get(0);
		
		if (!codeExists(code)) {
			code = "404";
		}
		
		String url = HTTP_CAT + code + ".jpg";
		
		MessageEmbed httpEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("HTTP " + code, url)
				.setImage(url)
				.build();
		message.getChannel().sendMessageEmbeds(httpEmbed).queue();
	}

	@Override
	public String getDescription() {
		return "Shows you the description of an HTTP code, along with an HTTP cat";
	}

	@Override
	public String getUsage() {
		return "http 404";
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
		String[] aliases = {"httpcat", "httpcode"};
		return aliases;
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}

}
