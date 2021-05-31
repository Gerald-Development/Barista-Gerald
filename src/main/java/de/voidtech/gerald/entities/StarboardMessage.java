package main.java.de.voidtech.gerald.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "StarboardMessage")

public class StarboardMessage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column
	private String originMessageID;
	
	@Column
	private String selfMessageID;
	
	@Column
	private long serverID;
	
	@Column
	private long starCount;
	
	@Deprecated
	//ONLY FOR HIBERNATE, DO NOT USE
	StarboardMessage() {
	}
	
	public StarboardMessage(String originMessageID, String selfMessageID, long serverID, long starCount)
	{
		this.originMessageID = originMessageID;
		this.selfMessageID = selfMessageID;
		this.serverID = serverID;
		this.starCount = starCount;
	}
	
	public String getOriginMessageID() {
		return originMessageID;
	}
	
	public void setOriginMessageID(String ID) {
		this.originMessageID = ID;
	}
	
	public String getSelfMessageID() {
		return selfMessageID;
	}
	
	public void setSelfMessageID(String ID) {
		this.selfMessageID = ID;
	}
	
	public long getServerID() {
		return serverID;
	}
	
	public void setServerID(long ID) {
		this.serverID = ID;
	}
	
	public long getStarCount() {
		return starCount;
	}
	
	public void setStarCount(long count) {
		this.starCount = count;
	}
	
}