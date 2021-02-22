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
	private int count; 
	
	@Column
	private String lastUser; 
	
	@Deprecated
	//ONLY FOR HIBERNATE, DO NOT USE
	CountingChannel() {
	}
	
	public CountingChannel(String channelID, int count, String lastUser)
	{
	  this.channelID = channelID;
	  this.count = count;
	  this.lastUser = lastUser;
	  
	}
	
	public String getCountingChannel() {
		return channelID;
	}

	public void setCountingChannel(String newChannelID) {
		this.channelID = newChannelID;
	}
	
	public int getChannelCount() {
		return this.count;
	}
	
	public void setChannelCount(int newCount) {
		this.count = newCount;
	}
	
	public String getLastUser() {
		return lastUser;
	}
	
	public void setLastUser(String newUser) {
		this.lastUser = newUser;
	}
	
}