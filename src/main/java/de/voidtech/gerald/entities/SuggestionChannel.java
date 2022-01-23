package main.java.de.voidtech.gerald.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "suggestionchannel")
public class SuggestionChannel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column
	private String channelID;
	
	@Column
	private String voteRole;
	
	@Column
	private long serverID;
	
	@Deprecated
	//ONLY FOR HIBERNATE, DO NOT USE
	SuggestionChannel() {
	}
	
	public SuggestionChannel(long serverID, String channelID)
	{
	  this.channelID = channelID;
	  this.serverID = serverID;
	}
	
	public boolean voteRoleRequired() {
		return this.voteRole != null;
	}
	
	public String getVoteRoleID() {
		return this.voteRole;
	}
	
	public void setVoteRole(String roleID) {
		this.voteRole = roleID;
	}
	
	public String getSuggestionChannel() {
		return channelID;
	}
	
	public long getServerID() {
		return serverID;
	}
	
	public void setServerID(long newServerID) {
		this.serverID = newServerID;
	}

	public void setSuggestionChannel(String newChannelID) {
		this.channelID = newChannelID;
	}
	
}