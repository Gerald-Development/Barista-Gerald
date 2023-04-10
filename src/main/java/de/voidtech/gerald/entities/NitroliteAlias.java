package main.java.de.voidtech.gerald.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "nitrolitealias")
public class NitroliteAlias {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column
	long serverID;
	
	@Column
	String aliasName;
	
	@Column
	String emoteID;
	
	@Deprecated
	//ONLY FOR HIBERNATE, DO NOT USE
	NitroliteAlias() {
	}
	
	public NitroliteAlias(long serverID, String aliasName, String emoteID)
	{
		this.serverID = serverID;
		this.aliasName = aliasName;
		this.emoteID = emoteID;
	}

	public void setServer(long newID) {
		this.serverID = newID;
	}

	public long getServer() {
		return this.serverID;
	}
	
	public void setAliasName(String name) {
		this.aliasName = name;
	}
	
	public String getAliasName() {
		return this.aliasName;
	}
	
	public void setEmoteID(String id) {
		this.emoteID = id;
	}
	
	public String getEmoteID() {
		return this.emoteID;
	}
}