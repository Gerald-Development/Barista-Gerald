package main.java.de.voidtech.gerald.entities;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

public class SuggestionEmote {
	
	private static final SuggestionEmote PURPLE = new SuggestionEmote("U+1f7e3", new Color(168, 144, 216, 255), ":purple_circle:");
	private static final SuggestionEmote BLUE = new SuggestionEmote("U+1f535", new Color(90, 175, 240, 255), ":blue_circle:");
	private static final SuggestionEmote GREEN = new SuggestionEmote("U+1f7e2", new Color(124, 176, 86, 255), ":green_circle:");
	private static final SuggestionEmote ORANGE = new SuggestionEmote("U+1f7e0", new Color(241, 141, 0, 255), ":orange_circle:");
	private static final SuggestionEmote RED = new SuggestionEmote("U+1f534", new Color(217, 41, 64, 255), ":red_circle:");
	
	private static final List<SuggestionEmote> EMOTE_LIST = Arrays.asList(new SuggestionEmote[] {BLUE, GREEN, ORANGE, RED, PURPLE});
	
	private String name;
	private String emote;
	private Color colour;
	
	SuggestionEmote(String name, Color colour, String emote) {
		this.name = name;
		this.emote = emote;
		this.colour = colour;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getEmote() {
		return this.emote;
	}
	
	public Color getColour() {
		return this.colour;
	}
	
	public static SuggestionEmote GetEmoteFromUnicode(String unicode) {
		return EMOTE_LIST.stream().filter(s -> s.getName().equals(unicode)).findFirst().orElse(null);
	}
}
