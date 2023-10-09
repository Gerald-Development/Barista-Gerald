package main.java.de.voidtech.gerald.commands;

public enum CommandCategory {
	ACTIONS("actions", "For hugging, slapping and more", ":heart:"),
    FUN("fun", "Silly goofy fun and games", ":video_game:"),
    INVISIBLE("invisible", "Tools for the developers", ":shushing_face:"), //Only visible and usable by bot masters
    INFO("information", "Random helpful info 'n' such", ":books:"),
    MANAGEMENT("management", "Configure Gerald in your server", ":closed_lock_with_key:"),
    UTILS("utils", "Handy dandy tools", ":desktop:");
 
    private final String category;
    private final String iconName;
    private final String description;

    CommandCategory(String category, String description, String iconName) {
        this.description = description;
    	this.category = category;
    	this.iconName = iconName;
	}
    
    public String getCategory() {
        return category;
    }
    
    public String getIcon() {
    	return iconName;
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        return this.category;
    }
}