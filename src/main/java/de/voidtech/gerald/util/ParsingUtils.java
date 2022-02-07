package main.java.de.voidtech.gerald.util;

import main.java.de.voidtech.gerald.commands.CommandContext;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParsingUtils {
	
	private static final Pattern HEX_PATTERN = Pattern.compile("^([a-fA-F0-9]{6})$"); 
	
	public static boolean isInteger(String str) {
	    if (str == null) {
	        return false;
	    }
	    int length = str.length();
	    if (length == 0) {
	        return false;
	    }
	    int i = 0;
	    if (str.charAt(0) == '-') {
	        if (length == 1) {
	            return false;
	        }
	        i = 1;
	    }
	    for (; i < length; i++) {
	        char c = str.charAt(i);
	        if (c < '0' || c > '9') {
	            return false;
	        }
	    }
	    return true;
	}
	
	public static String filterSnowflake(String inputString) {
		return inputString.replaceAll("([^0-9])", "");
	}
	
	public static Member getMember(CommandContext context, List<String> args) {
		
		if (args.size() > 0) {
			String memberID = ParsingUtils.filterSnowflake(args.get(0));
			Member member = context.getGuild().retrieveMemberById(memberID).complete();
			if (member != null) {
				return member;
			}
		} else {
			return context.getMentionedMembers().size() >= 1//
					   ? context.getMentionedMembers().get(0)//
					   : context.getMember();
		}
		return context.getMember();
	}
	
	public static boolean isSnowflake(String input) {
		return isInteger(input) && input.length() == 18;
	}
	
	public static boolean isHexadecimal(String input) {
		Matcher hexMatcher = HEX_PATTERN.matcher(input);
		return hexMatcher.find();
	}
}
