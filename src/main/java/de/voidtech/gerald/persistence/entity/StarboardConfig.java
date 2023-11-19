package main.java.de.voidtech.gerald.persistence.entity;

import javax.persistence.*;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "StarboardConfig")
public class StarboardConfig {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(unique = true, name="starboardchannel")
	private String starboardChannel; 
	
	@Column(name="serverid")
	private long serverID;
	
	@Column(name="requiredstarcount")
	private long requiredStarCount;
	
	@Column(name="ignoredchannels")
	private String ignoredChannels;
	
	@Deprecated
	//ONLY FOR HIBERNATE, DO NOT USE
	StarboardConfig() {
	}
	
	public StarboardConfig(long serverID, String channelID, long requiredStarCount, String ignoredChannels)
	{
		this.serverID = serverID;
		this.starboardChannel = channelID;
		this.requiredStarCount = requiredStarCount;
		this.ignoredChannels = ignoredChannels;
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
	
	public List<String> getIgnoredChannels() {
		return ignoredChannels == null ? null : Arrays.asList(ignoredChannels.split(","));
	}
	
	public void setIgnoredChannels(List<String> newList) {
		StringBuilder newListCompiled = new StringBuilder();
		for (String item : newList) 
			newListCompiled.append(item).append(",");
		this.ignoredChannels = newListCompiled.toString();
	}
}