package main.java.de.voidtech.gerald.routines;

public enum RoutineCategory {
    FUN("fun", ":video_game:"), 
    UTILS("utils", ":desktop:");
 
    private final String category;
    private final String iconName;
 
    RoutineCategory(String category, String iconName) {
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