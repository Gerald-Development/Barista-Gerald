package main.java.de.voidtech.gerald.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tunnel")

public class Tunnel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column
	private String sourceChannelID; 
	
	@Column
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