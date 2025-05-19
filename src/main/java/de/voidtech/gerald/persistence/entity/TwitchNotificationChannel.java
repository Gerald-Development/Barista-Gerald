package main.java.de.voidtech.gerald.persistence.entity;

import javax.persistence.*;

@Entity
@Table(name = "TwitchNotificationChannel")

public class TwitchNotificationChannel {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	@Column(name="channelid")
	private String channelID; 
	
	@Column(name="streamername")
	private String streamerName;
	
	@Column(name="notificationmessage")
	private String notificationMessage;
	
	@Column(name="serverid")
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