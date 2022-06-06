package main.java.de.voidtech.gerald.commands.utils;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Command
public class TranslateCommand extends AbstractCommand {
    
	private static final String TRANSLATE_URL = "https://libretranslate.de/translate";
	
    private HashMap<String, String> getLanguages() {
    	HashMap<String, String> languages = new HashMap<String, String>();
    	languages.put("vi", "Vietnamese");
    	languages.put("id", "Indonesian");
    	languages.put("pt", "Portugese");
    	languages.put("ja", "Japanese");
    	languages.put("it", "Italian");
    	languages.put("ru", "Russian");
    	languages.put("es", "Spanish");
    	languages.put("tr", "Turkish");
    	languages.put("en", "English");
    	languages.put("zh", "Chinese");
    	languages.put("ar", "Arabic");
    	languages.put("fr", "French");
    	languages.put("de", "German");
    	languages.put("ko", "Korean");
    	languages.put("pl", "Polish");
    	languages.put("hi", "Hindi");
    	return languages;
    }
	
	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		String text = "";
		if (args.isEmpty()) {
			if (context.getMessage().getReferencedMessage() == null)
				context.reply("**You need to either reply to a message to be translated or supply some text to be translated!**");
			else text = context.getMessage().getReferencedMessage().getContentRaw();
			sendTranslation(text, "en", context);
		} else {
			if (args.get(0).equals("languages")) {
				sendLanguagesList(context);
				return;
			}
			String language = "";
			if (getLanguages().keySet().contains(args.get(0).toLowerCase()))
				language = args.get(0).toLowerCase(); 
			
			if (context.getMessage().getReferencedMessage() == null) {
				if (args.size() == 1) 
					context.reply("**You need to either reply to a message to be translated or supply some text to be translated!**");
				else {
					if (language.equals("")) text = String.join(" ", args);
					else {
						List<String> argsMutable = args.stream().collect(Collectors.toList());
						argsMutable.remove(0);
						text = String.join(" ", argsMutable);
					}
				}
			} else text = context.getMessage().getReferencedMessage().getContentRaw();
			
			if (language.equals("")) language = "en";
			sendTranslation(text, language, context);
		}
	}
	
	private void sendLanguagesList(CommandContext context) {
		String languagesList = "";
		HashMap<String, String> languages = getLanguages();
		
		for (String languageCode : languages.keySet()) {
			languagesList += "`" + languageCode + "` - **" + languages.get(languageCode) + "**\n";
		}
		
		MessageEmbed languageListEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("Language Codes List")
				.setDescription(languagesList)
				.build();
		context.reply(languageListEmbed);
		
	}

	private void sendTranslation(String text, String language, CommandContext context) {
		JSONObject translation = getTranslation(text, language);
		String translationText = (translation == null ? "Unable to translate" : translation.getString("translatedText"));
		String sourceLanguage = getLanguages().get(translation.getJSONObject("detectedLanguage").getString("language"));
		MessageEmbed translationEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("Translation", "https://libretranslate.de/")
				.addField("Original Text (Detected " + sourceLanguage + ")", text, false)
				.addField("Translation into " + getLanguages().get(language), translationText, false)
				.setFooter("Translated by libretranslate.de")
				.build();
		context.reply(translationEmbed);
	}

	private JSONObject getTranslation(String textToBeTranslated, String language) {
		JSONObject payload = new JSONObject();
		payload.put("q", textToBeTranslated);
		payload.put("source", "auto");
		payload.put("target", language);
		payload.put("format", "text");
		return getTranslatedResponse(payload.toString());
	}
	
	private JSONObject getTranslatedResponse(String payload) {
		try {
			URL requestURL = new URL(TRANSLATE_URL);
			HttpURLConnection con = (HttpURLConnection) requestURL.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json; utf-8");
			con.setDoOutput(true);
			con.setRequestProperty("Accept", "application/json");
			
			try (OutputStream os = con.getOutputStream()) {
				byte[] input = payload.getBytes("utf-8");
				os.write(input, 0, input.length);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try (OutputStream os = con.getOutputStream(); BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
				String response = in.lines().collect(Collectors.joining());
				JSONObject jsonResponse = new JSONObject(response);
				return jsonResponse;
							
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			con.disconnect();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}

	@Override
	public String getDescription() {
		return "Translate messages and text into a variety of languages."
				+ " Reply to a message with the translate command and an optional language choice to translate the message!"
				+ " Or, supply your own text to be translated.";
	}

	@Override
	public String getUsage() {
		return "translate [language] [text]"
				+ "translate [text] (Language will be english here)";
	}

	@Override
	public String getName() {
		return "translate";
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
		return false;
	}

	@Override
	public String[] getCommandAliases() {
		return new String[] {"tl"};
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
