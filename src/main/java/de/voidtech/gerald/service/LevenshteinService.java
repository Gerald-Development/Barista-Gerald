package main.java.de.voidtech.gerald.service;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.GlobalConstants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

//Heavily inspired by https://www.baeldung.com/java-levenshtein-distance
@Service
public class LevenshteinService {
	
    public int costOfSubstitution(char first, char second) {
        return first == second ? 0 : 1;
    }

    public int min(int... numbers) {
        return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
    }
    
    public int calculate(String primaryString, String secondaryString) {
        int[][] valueTable = new int[primaryString.length() + 1][secondaryString.length() + 1];

        for (int primaryChars = 0; primaryChars <= primaryString.length(); primaryChars++) {
            for (int secondaryChars = 0; secondaryChars <= secondaryString.length(); secondaryChars++) {
                if (primaryChars == 0) valueTable[primaryChars][secondaryChars] = secondaryChars;
                else if (secondaryChars == 0) valueTable[primaryChars][secondaryChars] = primaryChars;
                else {
                    valueTable[primaryChars][secondaryChars] = min(valueTable[primaryChars - 1][secondaryChars - 1] 
                     + costOfSubstitution(primaryString.charAt(primaryChars - 1), secondaryString.charAt(secondaryChars - 1)), 
                      valueTable[primaryChars - 1][secondaryChars] + 1, valueTable[primaryChars][secondaryChars - 1] + 1);
                }
            }
        }
        return valueTable[primaryString.length()][secondaryString.length()];
    }

	public MessageEmbed createLevenshteinEmbed(List<String> possibleOptions) {
		EmbedBuilder levenshteinResultEmbed = new EmbedBuilder()
				.setColor(Color.RED)
				.setTitle("That's not a command!", GlobalConstants.LINKTREE_URL)
				.addField("Is this what you meant?", "`" + String.join("`, `", possibleOptions) + "`", false);
		return levenshteinResultEmbed.build();
	}
}