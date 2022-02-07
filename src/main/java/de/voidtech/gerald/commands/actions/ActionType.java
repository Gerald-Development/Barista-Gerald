package main.java.de.voidtech.gerald.commands.actions;

public enum ActionType {
	CUDDLE("cuddle"),
	HUG("hug"),
	KISS("kiss"),
	NOM("nom"),
	PAT("pat"),
	POKE("poke"),
	SLAP("slap"),
	TICKLE("tickle");
 
    private final String type;
 
    ActionType(String type) {
    	this.type = type;
	}
    
    public String getType() {
        return type;
    }
}