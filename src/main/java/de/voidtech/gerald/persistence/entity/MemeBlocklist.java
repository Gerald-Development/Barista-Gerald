package main.java.de.voidtech.gerald.persistence.entity;

import javax.persistence.*;

@Entity
@Table(name = "memeblocklist")

public class MemeBlocklist {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	@Column(name="serverid")
	private long serverID;
	
	@Column(name="blocklist")
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