package main.java.de.voidtech.gerald.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "leveluprole")
public class LevelUpRole {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column
	private String roleID;
	
	@Column
	private long serverID;
	
	@Column
	private long level;
	
	@Deprecated
	//ONLY FOR HIBERNATE, DO NOT USE
	LevelUpRole() {
	}
	
	public LevelUpRole(String roleID, long serverID, long level) {
		this.roleID = roleID;
		this.serverID = serverID;
		this.level = level;
	}
	
	public String getRoleID() {
		return this.roleID;
	}
	
	public long getServerID() {
		return this.serverID;
	}
	
	public long getLevel() {
		return this.level;
	}

}
