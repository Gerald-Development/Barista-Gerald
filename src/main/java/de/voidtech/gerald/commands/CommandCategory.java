package main.java.de.voidtech.gerald.commands;

public enum CommandCategory {
	ACTIONS("actions", ":heart:"),
    FUN("fun", ":video_game:"), 
    INFO("information", ":books:"), 
    MANAGEMENT("management", ":closed_lock_with_key:"),
    UTILS("utils", ":desktop:");
 
    private String category;
    private String iconName;
 
    CommandCategory(String category, String iconName) {
    	this.category = category;
    	this.iconName = iconName;
	}
    
    public String getCategory() {
        return category;
    }
    
    public String getIcon() {
    	return iconName;
    }
}