package main.java.de.voidtech.gerald.routines.utils;

import main.java.de.voidtech.gerald.annotations.Routine;
import main.java.de.voidtech.gerald.persistence.entity.NitroliteEmote;
import main.java.de.voidtech.gerald.persistence.repository.NitroliteEmoteRepository;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import main.java.de.voidtech.gerald.routines.RoutineCategory;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Routine
public class NitroliteCollectorRoutine extends AbstractRoutine {

	@Autowired
	private NitroliteEmoteRepository repository;

	private static final Logger LOGGER = Logger.getLogger(NitroliteCollectorRoutine.class.getSimpleName());
	private static final Pattern EMOTE_PATTERN = Pattern.compile("^(<:[^:\\s]+:[0-9]+>|<a:[^:\\s]+:[0-9]+>)+$");
	
	private boolean isEmote(String word) {
		Matcher emoteMatcher = EMOTE_PATTERN.matcher(word);
		return emoteMatcher.find();
	}
	
	private boolean emoteNotInDatabase(String name, String id) {
		return repository.getEmoteByNameAndID(name, id) == null;
	}
	
	private boolean emoteIsAlreadyStored(String name, String id, JDA jda) {	
		if (jda.getEmojiById(id) == null) {
			return !emoteNotInDatabase(name, id);
		}
		return true;
	}
	
	private boolean isAnimated(String word) {
		return word.startsWith("<a:");
	}
	
	private String getEmoteName(String word) {
		List<String> components = Arrays.asList(word.split(":"));
		return components.get(1);
	}
	
	private String getEmoteID(String word) {
		List<String> components = Arrays.asList(word.split(":"));
		return ParsingUtils.filterSnowflake(components.get(2).substring(0, components.get(2).length() - 1));
	}
	
	private void storeNewEmote(String emoteName, String emoteID, boolean emoteIsAnimated) {
		repository.save(new NitroliteEmote(emoteName, emoteID, emoteIsAnimated));
		LOGGER.log(Level.INFO, "New emote '" + emoteName + "' Has been saved!");
	}
	
	private void parseNewEmote(String word, JDA jda) {
		boolean emoteIsAnimated = isAnimated(word);
		String emoteName = getEmoteName(word);
		String emoteID = getEmoteID(word);
		
		if (!emoteIsAlreadyStored(emoteName, emoteID, jda)) {
			storeNewEmote(emoteName, emoteID, emoteIsAnimated);
		}
	}

	@Override
	public void executeInternal(Message message) {
		List<String> words = Arrays.asList(message.getContentRaw().replaceAll("(?<! )<:", " <:").replaceAll(":>(?! )", ">:] ").split(" "));
		words.forEach(word -> {
			if (isEmote(word)) {
				parseNewEmote(word, message.getJDA());
			}
		});
	}

	@Override
	public String getName() {
		return "r-EmoteGrabber5000";
	}

	@Override
	public String getDescription() {
		return "Allows nitrolite to add non-cached emotes";
	}

	@Override
	public RoutineCategory getRoutineCategory() {
		return RoutineCategory.UTILS;
	}

	@Override
	public boolean allowsBotResponses() {
		return true;
	}

	@Override
	public boolean canBeDisabled() {
		return false;
	}

}
