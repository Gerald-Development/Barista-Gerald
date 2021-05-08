package main.java.de.voidtech.gerald.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "actionstats")

public class ActionStats {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column
	private String type; 
	
	@Column
	private String memberID; 
	
	@Column
	private long givenCount;
	
	@Column
	private long receivedCount;
	
	@Deprecated
	//ONLY FOR HIBERNATE, DO NOT USE
	ActionStats() {
	}
	
	public ActionStats(String type, String memberID, long givenCount, long receivedCount)
	{
	  this.type = type;
	  this.memberID = memberID;
	  this.givenCount = givenCount;
	  this.receivedCount = receivedCount;
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
}