package main.java.de.voidtech.gerald.commands;

public enum CommandCategory {
    FUN("Fun"), 
    INFO("Information"), 
    MANAGEMENT("Management"), 
    UTILS("Utilities");
 
    private String category;
 
    CommandCategory(String string) {
	}
 
    public String getCategory() {
        return category;
    }	
}