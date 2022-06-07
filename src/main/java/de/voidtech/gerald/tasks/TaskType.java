package main.java.de.voidtech.gerald.tasks;

public enum TaskType {
	REMIND_ME("remind_me");
 
    private final String type;
 
    TaskType(String type) {
    	this.type = type;
	}
    
    public String getType() {
        return type;
    }
}
