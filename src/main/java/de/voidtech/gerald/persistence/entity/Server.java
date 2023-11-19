package main.java.de.voidtech.gerald.persistence.entity;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "server")
public class Server 
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(unique=true, name="guildid")
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
	
	@Column(name="prefix")
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
	
	public void addToCommandBlacklist(String commandName) {
		this.commandBlacklist.add(commandName);
	}
	public void removeFromCommandBlacklist(String commandName) {
		this.commandBlacklist.remove(commandName);
	}
	
	public void clearCommandBlacklist() {
		this.commandBlacklist.clear();
	}

	public Set<String> getRoutineBlacklist() {
		if (this.routineBlacklist == null) return Collections.unmodifiableSet(new HashSet<String>());
		else return Collections.unmodifiableSet(this.routineBlacklist);
	}

	public void addToRoutineBlacklist(String routineName) {
		this.routineBlacklist.add(routineName);
	}
	public void removeFromRoutineBlacklist(String routineName) {
		this.routineBlacklist.remove(routineName);
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
}
