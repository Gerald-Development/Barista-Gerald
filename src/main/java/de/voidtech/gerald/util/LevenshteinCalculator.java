package main.java.de.voidtech.gerald.util;

import java.util.Arrays;

public class LevenshteinCalculator {

    public static int costOfSubstitution(char first, char second) {
        return first == second ? 0 : 1;
    }

    public static int min(int... numbers) {
        return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
    }

    public static int calculate(String primaryString, String secondaryString) {
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
}
