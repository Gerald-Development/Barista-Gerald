package main.java.de.voidtech.gerald.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "experience")
public class Experience {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column
	private String userID;
	
	@Column
	private long serverID;
	
	@Column
	private long messageCount;
	
	@Column
	private long experienceGainedToNextLevel;
	
	@Column
	private long level;
	
	@Column
	private long lastMessageTime;
	
	@Deprecated
	//ONLY FOR HIBERNATE, DO NOT USE
	Experience() {
	}
	
	public Experience(String userID, long serverID) {
		this.userID = userID;
		this.serverID = serverID;
		this.messageCount = 0;
		this.level = 0;
		this.lastMessageTime = 0;
		this.experienceGainedToNextLevel = 0;
	}
	
	public void setLevel(long level) {
		this.level = level;
	}
	
	public void setCurrentXP(long xp) {
		this.experienceGainedToNextLevel = xp;
	}
	
	public void setLastMessageTime(long lastMessageTime) {
		this.lastMessageTime = lastMessageTime;
	}
	
	public void incrementMessageCount() {
		this.messageCount++;
	}
	
	public long getLevel() {
		return this.level;
	}
	
	public long getCurrentExperience() {
		return this.experienceGainedToNextLevel;
	}
	
	public long getLastMessageTime() {
		return this.lastMessageTime;
	}
	
	public long getMessageCount() {
		return this.messageCount;
	}
	
	public long getServerID() {
		return this.serverID;
	}
	
	public String getUserID() {
		return this.userID;
	}
}
