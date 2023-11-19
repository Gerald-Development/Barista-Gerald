package main.java.de.voidtech.gerald.persistence.entity;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Entity
@Table(name = "serverexperienceconfig")
public class ServerExperienceConfig {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name="serverid")
	private long serverID;
	
	@Column(name="levelupmessagesenabled")
	private boolean levelUpMessagesEnabled;

	@Column(columnDefinition = "int default -1", name="rate")
	private int rate;
	
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
		this.rate = -1;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public boolean rateIsRandomised() {
		return this.rate == -1;
	}
	
	public boolean levelUpMessagesEnabled() {
		return this.levelUpMessagesEnabled;
	}
	
	public void setLevelUpMessagesEnabled(boolean enabled) {
		this.levelUpMessagesEnabled = enabled;
	}

	public int getExperienceIncrement() {
		return rateIsRandomised() ? new Random().nextInt(15) + 1 : this.rate;
	}

	public int getRate() {
		return this.rate;
	}
}
