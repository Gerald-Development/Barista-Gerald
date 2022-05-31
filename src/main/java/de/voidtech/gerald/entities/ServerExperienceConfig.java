package main.java.de.voidtech.gerald.entities;

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
@Table(name = "serverexperienceconfig")
public class ServerExperienceConfig {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column
	private long serverID;
	
	@Column
	private boolean levelUpMessagesEnabled;
	
	//TODO: Don't fetch EAGER for this.
	@ElementCollection(fetch = FetchType.EAGER)
	@Cascade(CascadeType.REMOVE)
	private Set<String> noExperienceChannels;
	
	@Deprecated
	//ONLY FOR HIBERNATE, DO NOT USE
	ServerExperienceConfig() {
	}
	
	public Set<String> getNoXPChannels() {
		if(this.noExperienceChannels == null) return new HashSet<String>();
		else return this.noExperienceChannels;
	}
	
	public void addNoExperienceChannel(String ID) {
		this.noExperienceChannels.add(ID);
	}
	
	public void removeNoExperienceChannel(String ID) {
		this.noExperienceChannels.remove(ID);
	}
	
	public void clearNoExperienceChannels() {
		this.noExperienceChannels.clear();
	}
	
	public ServerExperienceConfig(long serverID) {
		this.serverID = serverID;
		this.levelUpMessagesEnabled = false;
	}
	
	public boolean levelUpMessagesEnabled() {
		return this.levelUpMessagesEnabled;
	}
	
	public void setLevelUpMessagesEnabled(boolean enabled) {
		this.levelUpMessagesEnabled = enabled;
	}
	
}