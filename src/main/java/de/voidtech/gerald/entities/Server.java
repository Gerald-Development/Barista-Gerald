package main.java.de.voidtech.gerald.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.Column;

@Entity
@Table(name = "server")
public class Server 
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column
	private String guildID;
	
	@Deprecated
	//ONLY FOR HIBERNATE, DO NOT USE
	Server() {
	}
	
	public Server(String guildID)
	{
		this.guildID = guildID;
	}
}
