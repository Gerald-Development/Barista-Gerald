package main.java.de.voidtech.gerald.persistence.entity;

import javax.persistence.*;

@Entity
@Table(name = "leveluprole")
public class LevelUpRole {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(name="roleid")
	private String roleID;
	
	@Column(name="serverid")
	private long serverID;
	
	@Column(name="level")
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
