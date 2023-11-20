package main.java.de.voidtech.gerald.persistence.entity;

import javax.persistence.*;

@Entity
@Table(name = "tunnel")

public class Tunnel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name="sourcechannelid")
	private String sourceChannelID; 
	
	@Column(name="destchannelid")
	private String destChannelID;
	
	@Deprecated
	//ONLY FOR HIBERNATE, DO NOT USE
	Tunnel() {
	}
	
	public Tunnel(String sourceChannelID, String destChannelID)
	{
	  this.sourceChannelID = sourceChannelID;
	  this.destChannelID = destChannelID;
	}
	
	public String getSourceChannel() {
		return sourceChannelID;
	}
	
	public String getDestChannel() {
		return destChannelID;
	}
	
	public void setSourceChannel(String newsourceChannelID) {
		this.sourceChannelID = newsourceChannelID;
	}
	
	public void setDestChannel(String newdestChannelID) {
		this.destChannelID = newdestChannelID;
	}
	
}