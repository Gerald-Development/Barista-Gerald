package main.java.de.voidtech.gerald.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "memeblocklist")

public class MemeBlocklist {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column
	private long serverID;
	
	@Column
	private String blocklist;
	
	@Deprecated
	//ONLY FOR HIBERNATE, DO NOT USE
	MemeBlocklist() {
	}
	
	public MemeBlocklist(long serverID, String blocklist)
	{
	  this.serverID = serverID;
	  this.blocklist = blocklist;
	}
	
	public long getServerID() {
		return serverID;
	}

	public void setServerID(long newServerID) {
		this.serverID = newServerID;
	}
	
	public String getBlocklist() {
		return blocklist;
	}

	public void setBlocklist(String newBlocklist) {
		this.blocklist = newBlocklist;
	}
	
}