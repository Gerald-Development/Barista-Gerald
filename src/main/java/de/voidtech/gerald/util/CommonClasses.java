package main.java.de.voidtech.gerald.util;

import org.springframework.stereotype.Component;

@Component
public class CommonClasses {
	public boolean isInteger(String str) {
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
	
	public String filterSnowflake(String inputString) {
		String outputString = inputString.replaceAll("([^0-9])", "");
		return outputString;
	}
}
