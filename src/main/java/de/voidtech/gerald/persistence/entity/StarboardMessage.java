package main.java.de.voidtech.gerald.persistence.entity;

import javax.persistence.*;

@Entity
@Table(name = "StarboardMessage")

public class StarboardMessage {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	@Column(unique = true, name="originalmessageid")
	private String originMessageID;
	
	@Column(unique = true, name="selfmessageid")
	private String selfMessageID;
	
	@Column(name="serverid")
	private long serverID;
	
	@Deprecated
	//ONLY FOR HIBERNATE, DO NOT USE
	StarboardMessage() {
	}
	
	public StarboardMessage(String originMessageID, String selfMessageID, long serverID)
	{
		this.originMessageID = originMessageID;
		this.selfMessageID = selfMessageID;
		this.serverID = serverID;
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
}