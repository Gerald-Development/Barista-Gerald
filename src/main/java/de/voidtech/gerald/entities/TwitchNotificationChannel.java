package main.java.de.voidtech.gerald.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "TwitchNotificationChannel")

public class TwitchNotificationChannel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column
	private String channelID; 
	
	@Column
	private String streamerName;
	
	@Column
	private String notificationMessage;
	
	@Column
	private long serverID;
	
	@Deprecated
	//ONLY FOR HIBERNATE, DO NOT USE
	TwitchNotificationChannel() {
	}
	
	public TwitchNotificationChannel(String channelID, String streamerName, String notificationMessage, long serverID)
	{
	  this.channelID = channelID;
	  this.streamerName = streamerName;
	  this.notificationMessage = notificationMessage;
	  this.serverID = serverID;
	}
	
	public String getChannelId() {
		return channelID;
	}

	public void setChannelId(String newChannelID) {
		this.channelID = newChannelID;
	}
	
	public String getStreamerName() {
		return streamerName;
	}

	public void setStreamerName(String streamerName) {
		this.streamerName = streamerName;
	}
	
	public String getNotificationMessage() {
		return notificationMessage;
	}

	public void setNotificationMessahe(String notificationMessage) {
		this.notificationMessage = notificationMessage;
	}
	
	public long getServerId() {
		return serverID;
	}

	public void setServerId(long serverID) {
		this.serverID = serverID;
	}
}