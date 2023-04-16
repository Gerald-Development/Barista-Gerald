package main.java.de.voidtech.gerald.commands.utils;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.json.JSONObject;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.service.LogService;
import main.java.de.voidtech.gerald.util.GeraldLogger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Command
public class TranslateCommand extends AbstractCommand {

	private static final String TRANSLATE_URL = "https://libretranslate.de/translate";
	private static final HashMap<String, String> TranslateLanguages;
	private static final GeraldLogger LOGGER = LogService.GetLogger(TranslateCommand.class.getSimpleName());

	static {
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
    	TranslateLanguages = languages;
	}
	
	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		String text;
		String language;
		//If no args are provided, translate a reply into english by default
		if (args.isEmpty()) {
			if (context.getMessage().getReferencedMessage() == null) {
				context.reply("**You need to either reply to a message or supply some text to be translated!**");
				return;
			} else text = context.getMessage().getReferencedMessage().getContentRaw();
			sendTranslation(text, "en", context);
		} else {
			//Check for language listing command first
			if (args.get(0).toLowerCase().equals("languages")) {
				sendLanguagesList(context);
				return;
			}
			//If args ARE provided, check for a reply with a language specified or some text with/without a language specified 
			if (context.getMessage().getReferencedMessage() == null) {
				//If only one argument is provided and that argument is a valid language, tell the shmoe they need to supply text
				if (TranslateLanguages.keySet().contains(args.get(0).toLowerCase())) {
					if (args.size() == 1) {
						context.reply("**You need to either reply to a message or supply some text to be translated!**");
						return;
					} else {
						language = args.get(0).toLowerCase();
						text = String.join(" ", args.subList(1, args.size()));	
					}
				//If the first argument is not a valid language, translate everything into english
				} else {
					language = "en";
					text = String.join(" ", args);
				}
				sendTranslation(text, language, context);
			} else {
				text = context.getMessage().getReferencedMessage().getContentRaw();
				if (TranslateLanguages.keySet().contains(args.get(0).toLowerCase())) language = args.get(0).toLowerCase();
				else language = "en";
				sendTranslation(text, language, context);
			}
		}
	}
	
	private void sendLanguagesList(CommandContext context) {
		StringBuilder languagesList = new StringBuilder();
		
		for (String languageCode : TranslateLanguages.keySet()) {
			languagesList.append("`").append(languageCode).append("` - **").append(TranslateLanguages.get(languageCode)).append("**\n");
		}
		
		MessageEmbed languageListEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("Language Codes List")
				.setDescription(languagesList.toString())
				.build();
		context.reply(languageListEmbed);
		
	}

	private void sendTranslation(String text, String language, CommandContext context) {
		JSONObject translation = getTranslation(text, language);
		String translationText = (translation == null ? "Unable to translate" : translation.getString("translatedText"));
		String sourceLanguage = TranslateLanguages.get(Objects.requireNonNull(translation).getJSONObject("detectedLanguage").getString("language"));
		MessageEmbed translationEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle("Translation", "https://libretranslate.de/")
				.addField("Original Text (Detected " + sourceLanguage + ")", text, false)
				.addField("Translation into " + TranslateLanguages.get(language), translationText, false)
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
				byte[] input = payload.getBytes(StandardCharsets.UTF_8);
				os.write(input, 0, input.length);
			} catch (IOException e) {
				e.printStackTrace();
			}

			try (OutputStream os = con.getOutputStream(); BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
				String response = in.lines().collect(Collectors.joining());
				return new JSONObject(response);

			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getMessage());
			}

			con.disconnect();
		} catch (IOException e1) {
			LOGGER.log(Level.SEVERE, e1.getMessage());
		}
		return null;
	}

	@Override
	public String getDescription() {
		return "Translate messages and text into a variety of languages."
				+ " Reply to a message with the translate command and an optional language choice to translate the message!"
				+ " Or, supply your own text to be translated. Use the 'languages'"
				+ " subcommand to see all the language codes for the supported languages!";
	}

	@Override
	public String getUsage() {
		return "translate [language] [text]"
				+ "translate [text] (Language will be english here)"
				+ "translate (reply to message to translate)"
				+ "translate languages";
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