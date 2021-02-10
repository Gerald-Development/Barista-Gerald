package main.java.de.voidtech.gerald.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	private List<String> channelWhitelist;
	
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
	
	public List<String> getChannelWhitelist() {
		if(this.channelWhitelist == null) return Collections.unmodifiableList(new ArrayList<String>());
		else return Collections.unmodifiableList(this.channelWhitelist);
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
}
