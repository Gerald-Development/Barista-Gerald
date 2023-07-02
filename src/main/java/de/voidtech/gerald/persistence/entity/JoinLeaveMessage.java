package main.java.de.voidtech.gerald.persistence.entity;

import javax.persistence.*;

@Entity
@Table(name = "joinleavemessage")

public class JoinLeaveMessage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column
	private long serverID; 
	
	@Column
	private String channelID; 
	
	@Column
	private String joinMessage; 
	
	@Column
	private String leaveMessage; 
	
	@Deprecated
	//ONLY FOR HIBERNATE, DO NOT USE
	JoinLeaveMessage() {
	}
	
	public JoinLeaveMessage(long serverID, String channelID, String joinMessage, String leaveMessage)
	{
		this.serverID = serverID;
		this.channelID = channelID;
	    this.joinMessage = joinMessage;
	    this.leaveMessage = leaveMessage;
	}
	
	public void setServerID(long serverID) {
		this.serverID = serverID;
	}
	
	public long getServerID() {
		return serverID;
	}
	
	public void setChannelID(String channelID) {
		this.channelID = channelID;
	}
	
	public String getChannelID() {
		return channelID;
	}
	
	public void setJoinMessage(String joinMessage) {
		this.joinMessage = joinMessage;
	}
	
	public String getJoinMessage() {
		return joinMessage;
	}
	
	public void setLeaveMessage(String leaveMessage) {
		this.leaveMessage = leaveMessage;
	}
	
	public String getLeaveMessage() {
		return leaveMessage;
	}
}