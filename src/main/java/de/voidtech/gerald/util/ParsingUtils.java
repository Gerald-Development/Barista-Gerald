package main.java.de.voidtech.gerald.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

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
		String outputString = inputString.replaceAll("([^0-9])", "");
		return outputString;
	}
	
	public static Member getMember(Message message, List<String> args) {
		
		if (args.size() > 0) {
			String memberID = ParsingUtils.filterSnowflake(args.get(0));
			Member member = message.getGuild().retrieveMemberById(memberID).complete();
			if (member != null) {
				return member;
			}
		} else {
			 Member member = message.getMentionedMembers().size() >= 1// 
						? message.getMentionedMembers().get(0)//
						: message.getMember();
			 return member;	
		}
		return message.getMember();
	}
	
	public static boolean isSnowflake(String input) {
		return isInteger(input) && input.length() == 18;
	}
	
	public static boolean isHexadecimal(String input) {
		Matcher hexMatcher = HEX_PATTERN.matcher(input);
		return hexMatcher.find();
	}
}
