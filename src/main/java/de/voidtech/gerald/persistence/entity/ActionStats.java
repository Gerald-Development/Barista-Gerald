package main.java.de.voidtech.gerald.persistence.entity;

import javax.persistence.*;

@Entity
@Table(name = "actionstats")
public class ActionStats {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name="type")
	private String type;

	@Column(name="memberid")
	private String memberID; 
	
	@Column(name="givencount")
	private long givenCount;
	
	@Column(name="receivedcount")
	private long receivedCount;
	
	@Column(name="serverid")
	long serverID;
	
	@Deprecated
	//ONLY FOR HIBERNATE, DO NOT USE
	ActionStats() {
	}
	
	public ActionStats(String type, String memberID, long givenCount, long receivedCount, long serverID)
	{
	  this.type = type;
	  this.memberID = memberID;
	  this.givenCount = givenCount;
	  this.receivedCount = receivedCount;
	  this.serverID = serverID;
	}
	
	public void setType(String newType) {
		this.type = newType;
	}
	
	public void setMember(String member) {
		this.memberID = member;
	}
	
	public void setGivenCount(long newCount) {
		this.givenCount = newCount;
	}
	
	public void setReceivedCount(long newCount) {
		this.receivedCount = newCount;
	}
	
	public void setServer(long newID) {
		this.serverID = newID;
	}
	
	public String getType() {
		return this.type;
	}
	
	public String getMember() {
		return this.memberID;
	}
	
	public long getGivenCount() {
		return this.givenCount;
	}
	
	public long getReceivedCount() {
		return this.receivedCount;
	}
	
	public long getServer() {
		return this.serverID;
	}
}