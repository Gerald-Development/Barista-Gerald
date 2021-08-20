package main.java.de.voidtech.gerald.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "countingchannel")

public class CountingChannel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column
	private String channelID; 
	
	@Column
	private String serverID;
	
	@Column
	private int countPosition; 
	
	@Column
	private String lastUser; 
	
	@Column
	private boolean hasReached69;
	
	@Column
	private boolean talkingAllowed;
	
	@Column 
	private int numberOfTimesItHasBeenNice;
	
	@Column
	private String lastCountMessageId;
	
	@Column
	private int lives;
	
	@Deprecated
	//ONLY FOR HIBERNATE, DO NOT USE
	CountingChannel() {
	}
	
	public CountingChannel(String channelID, String serverID)
	{
	  this.channelID = channelID;
	  this.serverID = serverID;
	  this.countPosition = 0;
	  this.lastUser = "";
	  this.hasReached69 = false;
	  this.numberOfTimesItHasBeenNice = 0;
	  this.talkingAllowed = true;
	  this.lastCountMessageId = "";
	  this.lives = 3;
	}
	
	public String getCountingChannel() {
		return channelID;
	}

	public void setCountingChannel(String newChannelID) {
		this.channelID = newChannelID;
	}
	
	public String getServerID() {
		return serverID;
	}

	public void setServerID(String newServerID) {
		this.serverID = newServerID;
	}
	
	public int getChannelCount() {
		return this.countPosition;
	}
	
	public void setChannelCount(int newCount) {
		this.countPosition = newCount;
	}
	
	public String getLastUser() {
		return lastUser;
	}
	
	public void setLastUser(String newUser) {
		this.lastUser = newUser;
	}
	
	public boolean hasReached69() {
		return this.hasReached69;
	}
	
	public void setReached69(boolean newState) {
		this.hasReached69 = newState;
	}	
	
	public int get69ReachedCount() {
		return this.numberOfTimesItHasBeenNice;
	}
	
	public void setNumberOfTimes69HasBeenReached(int newCount) {
		this.numberOfTimesItHasBeenNice = newCount;
	}
	
	public boolean talkingIsAllowed() {
		return this.talkingAllowed;
	}
	
	public void setIsTalkingAllowed(boolean isAllowed) {
		this.talkingAllowed = isAllowed;
	}
	
	public String getLastCountMessageId() {
		return this.lastCountMessageId;
	}
	
	public void setLastCountMessageId(String messageId) {
		this.lastCountMessageId = messageId;
	}
	
	public int getLives() {
		return this.lives;
	}
	
	public void removeLife() {
		this.lives = this.lives - 1;
	}
	
	public void resetLives() {
		this.lives = 3;
	}
}