package main.java.de.voidtech.gerald.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "StarboardConfig")

public class StarboardConfig {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	//TODO: REVIEW Should this be unique?
	@Column
	private String starboardChannel; 
	
	@Column
	private long serverID;
	
	@Column
	private long requiredStarCount;
	
	@Deprecated
	//ONLY FOR HIBERNATE, DO NOT USE
	StarboardConfig() {
	}
	
	public StarboardConfig(long serverID, String channelID, long requiredStarCount)
	{
		this.serverID = serverID;
		this.starboardChannel = channelID;
		this.requiredStarCount = requiredStarCount;
	}	
	
	public long getServerID() {
		return serverID;
	}
	
	public void setServerID(long newID) {
		this.serverID = newID;
	}
	
	public String getChannelID() {
		return starboardChannel;
	}
	
	public void setChannelID(String newID) {
		this.starboardChannel = newID;
	}
	
	public long getRequiredStarCount() {
		return requiredStarCount;
	}
	
	public void setRequiredStarCount(long newCount) {
		this.requiredStarCount = newCount;
	}
}