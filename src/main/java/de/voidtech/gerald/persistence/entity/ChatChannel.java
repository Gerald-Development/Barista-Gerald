package main.java.de.voidtech.gerald.persistence.entity;

import javax.persistence.*;

@Entity
@Table(name = "chatchannel")

public class ChatChannel {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	@Column(name="channelid")
	private String channelID; 
	
	@Deprecated
	//ONLY FOR HIBERNATE, DO NOT USE
	ChatChannel() {
	}
	
	public ChatChannel(String channelID)
	{
	  this.channelID = channelID;
	}
	
	public String getChatChannel() {
		return channelID;
	}

	public void setChatChannel(String newChannelID) {
		this.channelID = newChannelID;
	}
	
}