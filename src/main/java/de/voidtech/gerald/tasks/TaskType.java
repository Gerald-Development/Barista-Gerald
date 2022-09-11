package main.java.de.voidtech.gerald.tasks;

public enum TaskType {
	REMIND_ME("remind_me"),
	INSPIRO_DAILY_MESSAGE("inspiro_daily");
 
    private final String type;
 
    TaskType(String type) {
    	this.type = type;
	}
    
    public String getType() {
        return type;
    }
}
