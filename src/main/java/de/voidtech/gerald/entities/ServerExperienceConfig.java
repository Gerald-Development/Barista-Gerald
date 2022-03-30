package main.java.de.voidtech.gerald.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "serverexperienceconfig")
public class ServerExperienceConfig {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column
	private long serverID;
	
	@Column
	private boolean levelUpMessagesEnabled;
	
	@Deprecated
	//ONLY FOR HIBERNATE, DO NOT USE
	ServerExperienceConfig() {
	}
	
	public ServerExperienceConfig(long serverID) {
		this.serverID = serverID;
		this.levelUpMessagesEnabled = true;
	}
	
	public boolean levelUpMessagesEnabled() {
		return this.levelUpMessagesEnabled;
	}
	
	public void setLevelUpMessagesEnabled(boolean enabled) {
		this.levelUpMessagesEnabled = enabled;
	}
	
}