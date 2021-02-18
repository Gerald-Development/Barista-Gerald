package main.java.de.voidtech.gerald.commands;

public enum CommandCategory {
	ACTIONS("actions"),
    FUN("fun"), 
    INFO("information"), 
    MANAGEMENT("management"),
    UTILS("utils");
 
    private String category;
 
    CommandCategory(String category) {
    	this.category = category;
	}
    
    public String getCategory() {
        return category;
    }	
}