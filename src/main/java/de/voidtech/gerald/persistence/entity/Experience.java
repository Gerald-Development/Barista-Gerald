package main.java.de.voidtech.gerald.persistence.entity;

import javax.persistence.*;

@Entity
@Table(name = "experience")
public class Experience {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name="userid")
	private String userID;
	
	@Column(name="serverid")
	private long serverID;
	
	@Column(name="messagecount")
	private long messageCount;
	
	@Column(name="level")
	private long level;
	
	@Column(name="lastmessagetime")
	private long lastMessageTime;
	
	@Column(name="totalexperience")
	private long totalExperience;
	
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
	}
	
	public void setLevel(long level) {
		this.level = level;
	}
	
	public void incrementExperience(long xp) {
		this.totalExperience = this.totalExperience + xp;
	}

	public void setTotalExperience(long xp) {
		this.totalExperience = xp;
	}
	
	public void setLastMessageTime(long lastMessageTime) {
		this.lastMessageTime = lastMessageTime;
	}
	
	public void incrementMessageCount() {
		this.messageCount++;
	}
	
	public long getCurrentLevel() {
		return this.level;
	}
	
	public long getNextLevel() {
		return this.level + 1;
	}
	
	public long getTotalExperience() {
		return this.totalExperience;
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
