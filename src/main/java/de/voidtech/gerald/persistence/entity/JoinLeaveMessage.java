package main.java.de.voidtech.gerald.persistence.entity;

import javax.persistence.*;

@Entity
@Table(name = "joinleavemessage")

public class JoinLeaveMessage {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	@Column(name="serverid")
	private long serverID; 
	
	@Column(name="channelid")
	private String channelID; 
	
	@Column(name="joinmessage")
	private String joinMessage; 
	
	@Column(name="leavemessage")
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