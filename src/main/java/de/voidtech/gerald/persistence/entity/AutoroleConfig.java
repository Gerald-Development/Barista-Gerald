package main.java.de.voidtech.gerald.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "AutoroleConfig")

public class AutoroleConfig {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column
	private long serverID; 
	
	@Column
	private String roleID; 
	
	@Column
	private boolean availableForBots; 
	
	@Column
	private boolean availableForHumans; 
	
	@Deprecated
	//ONLY FOR HIBERNATE, DO NOT USE
	AutoroleConfig() {
	}
	
	public AutoroleConfig(long serverID, String roleID, boolean availableForBots, boolean availableForHumans)
	{
		this.serverID = serverID;
		this.roleID = roleID;
	    this.availableForBots = availableForBots;
	    this.availableForHumans = availableForHumans;
	}
	
	public void setServerID(long serverID) {
		this.serverID = serverID;
	}
	
	public long getServerID() {
		return serverID;
	}
	
	public void setRoleID(String roleID) {
		this.roleID = roleID;
	}
	
	public String getRoleID() {
		return roleID;
	}
	
	public void setHumanAvailability(boolean availablility) {
		this.availableForHumans = availablility;
	}
	
	public boolean isAvailableForHumans() {
		return availableForHumans;
	}
	
	public void setBotAvailability(boolean availablility) {
		this.availableForBots = availablility;
	}
	
	public boolean isAvailableForBots() {
		return availableForBots;
	}

}