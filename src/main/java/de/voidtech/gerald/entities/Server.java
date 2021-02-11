package main.java.de.voidtech.gerald.entities;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "server")
public class Server 
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column
	private String guildID;
	
	//TODO: Don't fetch EAGER for this.
	@ElementCollection(fetch = FetchType.EAGER)
	private Set<String> channelWhitelist;
	//TODO: Don't fetch EAGER for this.
	@ElementCollection(fetch = FetchType.EAGER)
	private Set<String> commandBlacklist;
	
	@Deprecated
	//ONLY FOR HIBERNATE, DO NOT USE
	Server() {
	}
	
	public Server(String guildID)
	{
		this.guildID = guildID;
	}
	
	public long getId() {
		return id;
	}
	
	public String getGuildID() {
		return guildID;
	}
	
	public Set<String> getChannelWhitelist() {
		if(this.channelWhitelist == null) return Collections.unmodifiableSet(new HashSet<String>());
		else return Collections.unmodifiableSet(this.channelWhitelist);
	}
	
	public void addToChannelWhitelist(String channelID) {
		this.channelWhitelist.add(channelID);
	}
	public void removeFromChannelWhitelist(String channelID) {
		this.channelWhitelist.remove(channelID);
	}
	public void clearChannelWhitelist() {
		this.channelWhitelist.clear();
	}

	public Set<String> getCommandBlacklist() {
		if(this.commandBlacklist == null) return Collections.unmodifiableSet(new HashSet<String>());
		else return Collections.unmodifiableSet(this.commandBlacklist);
	}
	
	public void addToCommandBlacklist(String channelID) {
		this.commandBlacklist.add(channelID);
	}
	public void removeFromCommandBlacklist(String channelID) {
		this.commandBlacklist.remove(channelID);
	}
}
