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

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
@Table(name = "server")
public class Server 
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	//THIS IS BEING TESTED. THIS IS NOT A FINAL SOLUTION
	@Column(unique = true)
	private String guildID;
	
	//TODO: Don't fetch EAGER for this.
	@ElementCollection(fetch = FetchType.EAGER)
	@Cascade(CascadeType.REMOVE)
	private Set<String> channelWhitelist;
	
	//TODO: Don't fetch EAGER for this.
	@ElementCollection(fetch = FetchType.EAGER)
	@Cascade(CascadeType.REMOVE)
	private Set<String> commandBlacklist;

	@ElementCollection(fetch = FetchType.EAGER)
	@Cascade(CascadeType.REMOVE)
	private Set<String> routineBlacklist;
	
	@Column
	private String prefix;
	
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

	public Set<String> getRoutineBlacklist() {
		if (this.routineBlacklist == null) return Collections.unmodifiableSet(new HashSet<String>());
		else return Collections.unmodifiableSet(this.routineBlacklist);
	}

	public void addToRoutineBlacklist(String guildID) {
		this.routineBlacklist.add(guildID);
	}
	public void removeFromRoutineBlacklist(String guildID) {
		this.routineBlacklist.remove(guildID);
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
}
